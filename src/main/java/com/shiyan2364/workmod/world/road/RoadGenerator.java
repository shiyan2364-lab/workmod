package com.shiyan2364.workmod.world.road;

import com.shiyan2364.workmod.api.AdventureStructure;
import com.shiyan2364.workmod.api.StructureRegistry;
import com.shiyan2364.workmod.api.StructureScale;
import com.shiyan2364.workmod.ui.MapManager;
import com.shiyan2364.workmod.ui.MapMarker;
import com.shiyan2364.workmod.world.breaks.BreakGenerator;
import com.shiyan2364.workmod.world.breaks.RoadBreakPoint;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 道路生成器（阶段1 重写版：真正自动铺路）
 * <p>
 * 原版 generateForStructure 只是打占位日志 "[Workmod] 道路生成占位"。
 * 本版实现：
 * 1. 用原版结构定位 API（ChunkGenerator.locateStructure）定位结构实例；
 * 2. 找出附近其它注册结构；
 * 3. 按规模决定路型（主路/支线/小径）与宽度；
 * 4. 调用 RoadNetwork.placeRoad 真正在世界中铺出方块。
 * <p>
 * 由 RoadAutoGenerator（服务端 tick）自动驱动。
 */
public final class RoadGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");

    /** 与相邻结构连线的最大距离 */
    private static final int LINK_RADIUS = 256;
    /** 每个结构最多连接的路数 */
    private static final int MAX_ROADS = 6;
    /** 宽度渐变段长度 */
    private static final int FADE = 16;

    private RoadGenerator() {
    }

    /**
     * 以 pos 为中心、radius 范围内查找注册表中已存在的结构实例。
     */
    public static List<AdventureStructure> findNearbyStructures(ServerWorld world, BlockPos pos, int radius) {
        List<AdventureStructure> result = new ArrayList<>();
        for (AdventureStructure structure : StructureRegistry.getAll()) {
            if (locate(world, structure, pos, radius) != null) {
                result.add(structure);
            }
        }
        return result;
    }

    /**
     * 为结构生成通往附近结构的道路。
     *
     * @param world     服务端世界
     * @param center    结构实例的起点坐标（locateStructure 定位结果）
     * @param structure 结构定义
     */
    public static void generateForStructure(ServerWorld world, BlockPos center, AdventureStructure structure) {
        String id = structure.id().toString();
        LOGGER.info("[Workmod] 为结构 {} @ {} 生成道路", id, center);

        RoadNetwork network = RoadNetwork.getInstance();
        // 结构入口圆形广场（说明书：交叉处/入口做圆形广场）
        int plazaRadius = RoadWidthCalculator.getWidth(RoadType.MAIN,
                magnificence(structure.scale(), structure.scale()));
        buildPlaza(world, center, structure, plazaRadius);

        // 小地图标记（供 MinimapHud 显示）
        try {
            MapMarker.MarkerType[] types = MapMarker.MarkerType.values();
            MapManager.addMarker(center,
                    types[structure.style().ordinal() % types.length],
                    structure.style().getDisplayName());
        } catch (Exception ignored) {
        }

        List<AdventureStructure> neighbors = findNearbyStructures(world, center, LINK_RADIUS);

        int count = 0;
        for (AdventureStructure nb : neighbors) {
            if (count >= MAX_ROADS) {
                break;
            }
            if (nb.id().equals(structure.id())) {
                continue;
            }
            BlockPos nbPos = locate(world, nb, center, LINK_RADIUS);
            if (nbPos == null) {
                continue;
            }
            // 已连过线则跳过（双向都算）
            if (hasSegmentBetween(id, nb.id().toString())) {
                continue;
            }

            RoadType type = pickType(structure.scale(), nb.scale());
            int startW = RoadWidthCalculator.getWidth(type, magnificence(structure.scale(), nb.scale()));
            int endW = Math.max(2, startW - 2); // 靠近建筑宽 → 中间窄

            RoadSegment seg = network.placeRoad(world, type, center, nbPos, startW, endW, FADE, structure.style().name());
            applyBreaks(world, seg);
            count++;
        }
        LOGGER.info("[Workmod] 结构 {} 完成 {} 条道路", id, count);
    }

    // ------------------------------------------------------------------
    // 阶段2：断路（悬崖/篝火桥/藤蔓/遗迹）
    // ------------------------------------------------------------------

    /**
     * 用 BreakGenerator 为路段生成断路点：
     * 1. 登记进 RoadNetwork（供存档/地图/修复使用）；
     * 2. 挖掉断路点周围的路面方块，形成真正的"断口"。
     */
    private static void applyBreaks(ServerWorld world, RoadSegment seg) {
        try {
            List<RoadBreakPoint> points = new BreakGenerator().generateBreaks(seg);
            if (points.isEmpty()) {
                return;
            }
            RoadNetwork network = RoadNetwork.getInstance();
            for (RoadBreakPoint bp : points) {
                network.addBreak(bp.getPos());
                digBreak(world, bp.getPos());
            }
            LOGGER.info("[Workmod] 路段 {} 生成 {} 处断路", seg.getId(), points.size());
        } catch (Exception e) {
            LOGGER.warn("[Workmod] 断路生成失败: {}", e.getMessage());
        }
    }

    /** 挖掉断点及其周围 3×3 的路面（仅地表一层），形成断口 */
    private static void digBreak(ServerWorld world, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int bx = pos.getX() + dx;
                int bz = pos.getZ() + dz;
                int by = world.getTopY(Heightmap.Type.MOTION_BLOCKING, bx, bz);
                world.setBlockState(new BlockPos(bx, by, bz), Blocks.AIR.getDefaultState(), 3);
            }
        }
    }

    // ------------------------------------------------------------------
    // 道路装饰：结构入口圆形广场
    // ------------------------------------------------------------------

    /** 在结构入口铺一个圆形广场（半径受宏伟度影响，限制 ≤8 防卡） */
    private static void buildPlaza(ServerWorld world, BlockPos center, AdventureStructure structure, int roadWidth) {
        RoadBlockTable.RoadStyle table = RoadBlockTable.getStyle(structure.style());
        int r = Math.min(8, Math.max(2, roadWidth / 4));
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > r * r) {
                    continue;
                }
                int bx = center.getX() + dx;
                int bz = center.getZ() + dz;
                int by = world.getTopY(Heightmap.Type.MOTION_BLOCKING, bx, bz);
                BlockPos p = new BlockPos(bx, by, bz);
                BlockState cur = world.getBlockState(p);
                if (cur.isOf(Blocks.WATER) || cur.isOf(Blocks.LAVA)) {
                    continue;
                }
                boolean edge = Math.abs(dx) >= r - 1 || Math.abs(dz) >= r - 1;
                world.setBlockState(p,
                        (edge ? table.edgeBlock() : table.mainBlock()).getDefaultState(), 3);
            }
        }
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    /** 用原版结构定位 API 找最近实例（与 /locate 同款） */
    private static BlockPos locate(ServerWorld world, AdventureStructure structure, BlockPos pos, int radius) {
        RegistryKey<Structure> key = RegistryKey.of(RegistryKeys.STRUCTURE, structure.id());
        Optional<RegistryEntry.Reference<Structure>> entry =
                world.getRegistryManager().get(RegistryKeys.STRUCTURE).getEntry(key);
        if (entry.isEmpty()) {
            return null;
        }
        try {
            com.mojang.datafixers.util.Pair<BlockPos, RegistryEntry<Structure>> found =
                    world.getChunkManager().getChunkGenerator()
                            .locateStructure(world, RegistryEntryList.of(entry.get()), pos, radius, false);
            return found == null ? null : found.getFirst();
        } catch (Exception e) {
            LOGGER.warn("[Workmod] 定位结构 {} 失败: {}", structure.id(), e.getMessage());
            return null;
        }
    }

    /** 两个结构之间是否已生成过 auto_ 路段 */
    private static boolean hasSegmentBetween(String a, String b) {
        String keyA = a.replace(':', '_');
        String keyB = b.replace(':', '_');
        for (RoadSegment seg : RoadNetwork.getInstance().getAllSegments()) {
            String name = seg.getId();
            if (name.startsWith("auto_") && name.contains(keyA) && name.contains(keyB)) {
                return true;
            }
        }
        return false;
    }

    /** 按规模定路型：有大建筑走主路，中等走支线，小走小径 */
    private static RoadType pickType(StructureScale a, StructureScale b) {
        if (a == StructureScale.LARGE || b == StructureScale.LARGE) {
            return RoadType.MAIN;
        }
        if (a == StructureScale.MEDIUM || b == StructureScale.MEDIUM) {
            return RoadType.BRANCH;
        }
        return RoadType.PATH;
    }

    /** 规模 → 宏伟度（1..10），取两端较大者 */
    private static int magnificence(StructureScale a, StructureScale b) {
        int m = Math.max(a.ordinal(), b.ordinal()) + 1; // TINY=1 … LARGE=4
        return Math.min(10, Math.max(1, m * 2));
    }
}
