package com.shiyan2364.workmod.world.mountains;

import com.shiyan2364.workmod.data.TerrainSavedData;
import com.shiyan2364.workmod.item.CurseItems;
import com.shiyan2364.workmod.item.curse.ExtremeProgress;
import com.shiyan2364.workmod.item.curse.ExtremeSeries;
import com.shiyan2364.workmod.world.treasures.TreasureAutoSpawner;
import com.shiyan2364.workmod.world.treasures.TreasurePoint.TreasureRarity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 高山登山道自动生成器（阶段6 新增）
 * <p>
 * MountainTrailGenerator 只生成登山道数据（之字形路径、宽度、宝藏稀有度），
 * 从不铺方块。本类补上实装：
 * 1. 在玩家周围选山脚点与山顶点（山顶取高处地形或直接抬升高度）；
 * 2. 生成登山道数据并存入 TerrainSavedData；
 * 3. 沿路径铺 1-2 格宽的石砖栈道，悬空段每隔数格立一根支撑柱；
 * 4. 山顶铺平台，20% 概率放宝箱（稀有度沿用生成数据）。
 * <p>
 * 每 tick 处理一批路径点，避免卡服。
 */
public final class MountainAutoGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");

    /** 每多少 tick 启动一条新登山道（200 tick = 10 秒） */
    private static final int TICK_INTERVAL = 200;
    /** 每 tick 铺的路径点数量 */
    private static final int POINTS_PER_TICK = 40;
    /** 距玩家距离范围 */
    private static final int MIN_DIST = 80;
    private static final int MAX_DIST = 150;
    /** 山顶相对山脚的目标抬升高度 */
    private static final int SUMMIT_RISE = 200;
    /** 山顶宝箱概率（20%） */
    private static final double TREASURE_CHANCE = 0.2;

    private static final Set<String> PROCESSED = new HashSet<>();
    private static final Random RANDOM = new Random();
    private static final String[] NAMES = {
            "无人知晓之径", "孤峰天梯", "远古登山道", "失落的栈道", "云海之脊"
    };

    /** 可放入山顶宝箱的诅咒饰品 */
    private static final Item[] CURSE_DROPS = {
            CurseItems.MAIN_RING, CurseItems.GREED_HAND, CurseItems.ENVY_EYE,
            CurseItems.WRATH_HAMMER, CurseItems.SLOTH_STONE, CurseItems.PRIDE_CROWN,
            CurseItems.LUST_FLOWER, CurseItems.FEAR_HEART
    };

    /** 当前铺路任务 */
    private static MountainTrailData current = null;
    private static int pointIndex = 0;
    private static boolean building = false;
    private static int tickCounter = 0;

    private MountainAutoGenerator() {
    }

    /** 服务端每 tick 调用一次（由 Workmod 注册） */
    public static void tick(ServerWorld world) {
        tickCounter++;
        if (building) {
            buildNext(world);
            return;
        }
        if (tickCounter % TICK_INTERVAL != 0) {
            return;
        }
        ServerPlayerEntity player = firstPlayer(world.getServer());
        if (player == null) {
            return;
        }
        BlockPos center = player.getBlockPos();

        // 随机山脚位置
        double angle = RANDOM.nextDouble() * Math.PI * 2;
        int dist = MIN_DIST + RANDOM.nextInt(MAX_DIST - MIN_DIST);
        int fx = center.getX() + (int) Math.round(Math.cos(angle) * dist);
        int fz = center.getZ() + (int) Math.round(Math.sin(angle) * dist);
        int fy = world.getTopY(Heightmap.Type.MOTION_BLOCKING, fx, fz);
        String key = (fx >> 4) + "," + (fz >> 4);
        if (PROCESSED.contains(key)) {
            return;
        }
        PROCESSED.add(key);

        // 山顶：沿随机水平方向延伸一段，取高处地表；若高差不足则直接抬升
        BlockPos foot = new BlockPos(fx, fy, fz);
        double summitAngle = RANDOM.nextDouble() * Math.PI * 2;
        int sx = fx + (int) Math.round(Math.cos(summitAngle) * 60);
        int sz = fz + (int) Math.round(Math.sin(summitAngle) * 60);
        int sy = world.getTopY(Heightmap.Type.MOTION_BLOCKING, sx, sz);
        if (sy - fy < 30) {
            sy = Math.min(world.getTopY(), fy + SUMMIT_RISE);
        }
        BlockPos summit = new BlockPos(sx, sy, sz);

        String name = NAMES[RANDOM.nextInt(NAMES.length)];
        MountainTrailData trail = new MountainTrailGenerator().generate(foot, summit, name);
        if (trail == null || trail.getPath().size() < 2) {
            return;
        }

        // 存档
        try {
            TerrainSavedData terrain = world.getPersistentStateManager().getOrCreate(
                    TerrainSavedData::fromNbt, TerrainSavedData::create, "workmod_terrain");
            terrain.addTrail(trail);
        } catch (Exception e) {
            LOGGER.warn("[Workmod] 登山道存档失败: {}", e.getMessage());
        }

        current = trail;
        pointIndex = 0;
        building = true;
        LOGGER.info("[Workmod] 登山道开始铺设: {} {} → {}", name, foot, summit);
    }

    /** 世界重载时重置 */
    public static void reset() {
        current = null;
        building = false;
        pointIndex = 0;
        PROCESSED.clear();
    }

    // ------------------------------------------------------------------
    // 铺设
    // ------------------------------------------------------------------

    private static void buildNext(ServerWorld world) {
        if (current == null) {
            building = false;
            return;
        }
        List<BlockPos> path = current.getPath();
        int width = Math.max(1, Math.min(2, current.getPathWidth()));
        int handled = 0;
        while (handled < POINTS_PER_TICK && pointIndex < path.size()) {
            BlockPos p = path.get(pointIndex);
            placePoint(world, p, width, pointIndex);
            pointIndex++;
            handled++;
        }
        if (pointIndex >= path.size()) {
            buildSummit(world, current);
            buildFootMarker(world, current);
            // 极致成就·苍穹：成功开辟一条登山道即推进"登顶高山"
            ExtremeProgress.addProgress(ExtremeSeries.SKY);
            LOGGER.info("[Workmod] 登山道铺设完成: {} ({})", current.getName(), path.size());
            current = null;
            building = false;
            pointIndex = 0;
        }
    }

    /** 铺一个路径点：表面方块 + 悬空段每隔 6 点立支撑柱 */
    private static void placePoint(ServerWorld world, BlockPos p, int width, int index) {
        if (!world.isChunkLoaded(p.getX() >> 4, p.getZ() >> 4)) {
            return;
        }
        int groundY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, p.getX(), p.getZ());
        // 表面（1-2 格宽）
        for (int dx = 0; dx < width; dx++) {
            BlockPos top = new BlockPos(p.getX() + dx, p.getY(), p.getZ());
            world.setBlockState(top, Blocks.STONE_BRICKS.getDefaultState(), 2);
            if (index % 6 == 0 && p.getY() - groundY > 1) {
                for (int y = p.getY() - 1; y >= groundY; y--) {
                    world.setBlockState(new BlockPos(p.getX() + dx, y, p.getZ()), Blocks.STONE.getDefaultState(), 2);
                }
            }
        }
    }

    /** 山顶平台 + 20% 宝箱 */
    private static void buildSummit(ServerWorld world, MountainTrailData trail) {
        BlockPos s = trail.getSummit();
        if (!world.isChunkLoaded(s.getX() >> 4, s.getZ() >> 4)) {
            return;
        }
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, s.getX(), s.getZ());
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = new BlockPos(s.getX() + dx, y, s.getZ() + dz);
                world.setBlockState(p, Blocks.STONE_BRICKS.getDefaultState(), 2);
                world.setBlockState(p.up(), Blocks.AIR.getDefaultState(), 2);
            }
        }
        if (RANDOM.nextDouble() < TREASURE_CHANCE) {
            int r = Math.max(0, Math.min(3, trail.getTreasureRarity()));
            TreasureRarity rarity = TreasureRarity.values()[r];
            try {
                BlockPos chestPos = new BlockPos(s.getX(), y + 1, s.getZ());
                TreasureAutoSpawner.placeTreasure(world, chestPos, rarity);
                // 40% 概率在山顶宝箱里追加一件诅咒饰品
                if (RANDOM.nextDouble() < 0.4
                        && world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                    Item curse = CURSE_DROPS[RANDOM.nextInt(CURSE_DROPS.length)];
                    chest.setStack(0, new ItemStack(curse));
                }
                LOGGER.info("[Workmod] 山顶宝箱: {} ({})", s, rarity.getDisplayName());
            } catch (Exception e) {
                LOGGER.warn("[Workmod] 山顶宝箱放置失败: {}", e.getMessage());
            }
        }
    }

    /** 山脚石碑（说明书：无人知晓 / 无人来过 / 无人离开） */
    private static void buildFootMarker(ServerWorld world, MountainTrailData trail) {
        BlockPos foot = trail.getFoot();
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, foot.getX(), foot.getZ());
        BlockPos signPos = new BlockPos(foot.getX(), y, foot.getZ());
        if (!world.isChunkLoaded(signPos.getX() >> 4, signPos.getZ() >> 4)) {
            return;
        }
        try {
            world.setBlockState(signPos, Blocks.OAK_SIGN.getDefaultState(), 2);
            if (world.getBlockEntity(signPos) instanceof SignBlockEntity sign) {
                sign.changeText(t -> t
                        .withMessage(0, Text.literal("无人知晓"))
                        .withMessage(1, Text.literal("无人来过"))
                        .withMessage(2, Text.literal("无人离开")), false);
                sign.markDirty();
            }
        } catch (Exception e) {
            LOGGER.warn("[Workmod] 山脚石碑失败: {}", e.getMessage());
        }
    }

    private static ServerPlayerEntity firstPlayer(MinecraftServer server) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        return players.isEmpty() ? null : players.get(0);
    }
}
