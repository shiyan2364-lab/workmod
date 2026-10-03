package com.shiyan2364.workmod.world.road;

import com.shiyan2364.workmod.api.StructureStyle;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 道路网络（阶段1 增强版）
 * <p>
 * 原版只登记路网数据（generateRoad / generatePath），不放置方块 —— 这就是"道路没有生成"的原因。
 * 本版保留全部原有公开 API，新增世界铺路能力：
 * - {@link #placeRoad(ServerWorld, RoadType, BlockPos, BlockPos, int, int, int, String)}：登记 + 铺方块一步到位
 * - {@link #placeSegment(ServerWorld, RoadSegment)}：把路段按路径点铺进世界（宽度渐变 + 横截面）
 */
public final class RoadNetwork {

    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final RoadNetwork INSTANCE = new RoadNetwork();

    public static RoadNetwork getInstance() {
        return INSTANCE;
    }

    private final List<RoadSegment> segments = new ArrayList<>();
    private final Random random = new Random();
    private int nextId = 1;
    private double breakChance = 0.08;
    private BlockPos lastBreak;

    public RoadNetwork() {
    }

    /** 清空路网（世界重载时调用） */
    public void reset() {
        segments.clear();
        nextId = 1;
        lastBreak = null;
    }

    /**
     * 【原版 API】登记一条路段并生成直线路径点（数据层，不放置方块）。
     *
     * @return 创建的路段（已登记进网络）
     */
    public RoadSegment generateRoad(RoadType type, BlockPos start, BlockPos end,
                                    int startWidth, int endWidth, int fadeDistance, String styleName) {
        RoadSegment seg = new RoadSegment("road_" + (nextId++), type, start, end,
                startWidth, endWidth, fadeDistance, styleName);
        List<BlockPos> path = generatePath(start, end);
        for (BlockPos p : path) {
            seg.addPoint(p);
        }
        segments.add(seg);
        return seg;
    }

    /** 【原版 API】两点间直线路径点（3D 线性插值） */
    private List<BlockPos> generatePath(BlockPos start, BlockPos end) {
        List<BlockPos> path = new ArrayList<>();
        int dx = end.getX() - start.getX();
        int dy = end.getY() - start.getY();
        int dz = end.getZ() - start.getZ();
        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        steps = Math.min(steps, 500);
        if (steps == 0) {
            return path;
        }
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            path.add(new BlockPos(
                    start.getX() + (int) Math.round(dx * t),
                    start.getY() + (int) Math.round(dy * t),
                    start.getZ() + (int) Math.round(dz * t)));
        }
        return path;
    }

    public List<RoadSegment> getAllSegments() {
        return new ArrayList<>(segments);
    }

    public boolean isOnExistingRoad(BlockPos pos) {
        for (RoadSegment seg : segments) {
            if (isPointNearSegment(pos, seg, 2)) {
                return true;
            }
        }
        return false;
    }

    /** 【原版 API，private】判断点是否靠近某路段 */
    private boolean isPointNearSegment(BlockPos pos, RoadSegment seg, int radius) {
        for (BlockPos p : seg.getPoints()) {
            if (Math.abs(p.getX() - pos.getX()) <= radius && Math.abs(p.getZ() - pos.getZ()) <= radius) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // 阶段1 新增：世界铺路
    // ------------------------------------------------------------------

    /**
     * 登记一条路段 + 生成路径点 + 在世界中铺出方块，一步到位。
     */
    public RoadSegment placeRoad(ServerWorld world, RoadType type, BlockPos start, BlockPos end,
                                 int startWidth, int endWidth, int fadeDistance, String styleName) {
        RoadSegment seg = generateRoad(type, start, end, startWidth, endWidth, fadeDistance, styleName);
        placeSegment(world, seg);
        return seg;
    }

    /**
     * 把路段按路径点铺进世界：
     * 宽度从 startWidth 渐变到 endWidth；横截面沿路径切线法向展开；
     * 中心用主方块、两侧用边缘方块、下方垫基底方块。
     */
    public void placeSegment(ServerWorld world, RoadSegment seg) {
        List<BlockPos> points = seg.getPoints();
        if (points.size() < 2) {
            return;
        }
        StructureStyle style;
        try {
            style = StructureStyle.valueOf(seg.getStyleName());
        } catch (IllegalArgumentException e) {
            style = StructureStyle.VILLAGE;
        }
        RoadBlockTable.RoadStyle table = RoadBlockTable.getStyle(style);
        int n = points.size();
        for (int i = 0; i < n; i++) {
            BlockPos p = points.get(i);
            BlockPos prev = points.get(Math.max(0, i - 1));
            BlockPos next = points.get(Math.min(n - 1, i + 1));
            double dirX = next.getX() - prev.getX();
            double dirZ = next.getZ() - prev.getZ();
            double len = Math.hypot(dirX, dirZ);
            if (len < 1e-6) {
                dirX = 1;
                len = 1;
            }
            double nx = -dirZ / len;
            double nz = dirX / len;

            double t = (double) i / (n - 1);
            int width = seg.getStartWidth()
                    + (int) Math.round((seg.getEndWidth() - seg.getStartWidth()) * t);
            if (width < 1) {
                width = 1;
            }
            int half = width / 2;
            for (int s = -half; s <= width - half; s++) {
                int bx = (int) Math.round(p.getX() + nx * s);
                int bz = (int) Math.round(p.getZ() + nz * s);
                int by = world.getTopY(Heightmap.Type.MOTION_BLOCKING, bx, bz);
                boolean edge = half >= 1 && (s == -half || s == width - half);
                placeColumn(world, bx, by, bz, edge ? table.edgeBlock() : table.mainBlock(), table.bridgeBlock());
                // 车辙：每 4 个点位、中心格铺灰化土，形成车辙条纹（说明书：路边有车辙）
                if (i % 4 == 0 && s >= -1 && s <= 1 && half >= 2) {
                    world.setBlockState(new BlockPos(bx, by, bz), Blocks.COARSE_DIRT.getDefaultState(), 3);
                }
            }
            // 路边火把装饰：每 16 个点位，两侧交替（说明书：路标火把）
            if (i % 16 == 0 && half >= 1) {
                int side = (i / 16) % 2 == 0 ? 1 : -1;
                int tx = (int) Math.round(p.getX() + nx * (half + 1) * side);
                int tz = (int) Math.round(p.getZ() + nz * (half + 1) * side);
                placeTorch(world, tx, tz);
            }
            // 路碑：每 32 个点位、两侧交替放石头+告示牌（路边有路碑）
            if (i % 32 == 0 && half >= 1) {
                int side = (i / 32) % 2 == 0 ? 1 : -1;
                int sx = (int) Math.round(p.getX() + nx * (half + 2) * side);
                int sz = (int) Math.round(p.getZ() + nz * (half + 2) * side);
                placeSign(world, sx, sz);
            }
            // 废墟装饰：每 48 个点位、路边放碎岩（说明书：路边有废墟装饰）
            if (i % 48 == 0 && half >= 1) {
                int side = (i / 48) % 2 == 0 ? 1 : -1;
                int rx = (int) Math.round(p.getX() + nx * (half + 2) * side);
                int rz = (int) Math.round(p.getZ() + nz * (half + 2) * side);
                placeRuins(world, rx, rz);
            }
        }
        LOGGER.info("[Workmod] 已铺路: {} ({} 点, 宽 {}→{}, 风格 {})",
                seg.getId(), points.size(), seg.getStartWidth(), seg.getEndWidth(), seg.getStyleName());
    }

    /** 放置一根柱子：地表替换为 top，地表下一格垫 base（水面/岩浆跳过） */
    private void placeColumn(ServerWorld world, int x, int y, int z, Block top, Block base) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState cur = world.getBlockState(pos);
        if (cur.isOf(Blocks.WATER) || cur.isOf(Blocks.LAVA)) {
            return;
        }
        world.setBlockState(pos, top.getDefaultState(), 3);
        BlockPos below = pos.down();
        BlockState belowState = world.getBlockState(below);
        if (!belowState.isAir() && !belowState.isOf(Blocks.WATER) && !belowState.isOf(Blocks.LAVA)) {
            world.setBlockState(below, base.getDefaultState(), 3);
        }
    }

    /** 路边火把：下方有实心方块才放（水面/岩浆/悬空跳过） */
    private void placeTorch(ServerWorld world, int x, int z) {
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        BlockState below = world.getBlockState(pos.down());
        if (below.isAir() || below.isOf(Blocks.WATER) || below.isOf(Blocks.LAVA)) {
            return;
        }
        if (!world.getBlockState(pos).isAir()) {
            return;
        }
        world.setBlockState(pos, Blocks.TORCH.getDefaultState(), 3);
    }

    /** 路碑：石头底座 + 告示牌（路边有路碑） */
    private static final String[] SIGN_TEXTS = {"古道", "旅者之路", "远古商道", "朝圣之径", "苍茫道"};

    private void placeSign(ServerWorld world, int x, int z) {
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
        BlockPos stonePos = new BlockPos(x, y, z);
        BlockPos signPos = stonePos.up();
        world.setBlockState(stonePos, Blocks.STONE.getDefaultState(), 3);
        world.setBlockState(signPos, Blocks.OAK_SIGN.getDefaultState(), 3);
        if (world.getBlockEntity(signPos) instanceof SignBlockEntity sign) {
            String text = SIGN_TEXTS[Math.floorMod(x + z, SIGN_TEXTS.length)];
            sign.changeText(t -> t.withMessage(0, Text.literal(text)), false);
            sign.markDirty();
        }
    }

    /** 路边废墟碎岩（圆石/苔藓圆石/安山岩/沙砾） */
    private static final Block[] RUIN_BLOCKS = {
            Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.ANDESITE, Blocks.GRAVEL
    };

    private void placeRuins(ServerWorld world, int x, int z) {
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        if (!world.getBlockState(pos).isAir()) {
            return;
        }
        Block ruin = RUIN_BLOCKS[Math.floorMod(x + z, RUIN_BLOCKS.length)];
        world.setBlockState(pos, ruin.getDefaultState(), 3);
    }

    /** 标记一个断路点（阶段2 用；登记到所有路段） */
    public void addBreak(BlockPos pos) {
        if (pos == null) {
            return;
        }
        for (RoadSegment seg : segments) {
            seg.addBreak(pos);
        }
        lastBreak = pos;
    }

    public BlockPos getLastBreak() {
        return lastBreak;
    }

    public double getBreakChance() {
        return breakChance;
    }

    public void setBreakChance(double chance) {
        this.breakChance = chance;
    }
}
