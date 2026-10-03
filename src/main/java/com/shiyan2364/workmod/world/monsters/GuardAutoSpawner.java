package com.shiyan2364.workmod.world.monsters;

import com.shiyan2364.workmod.world.road.RoadNetwork;
import com.shiyan2364.workmod.world.road.RoadSegment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 守卫自动生成器（阶段4 重写版）
 * <p>
 * 原版是空壳（tick 什么都不做）。本版实现：
 * 沿已生成的道路（RoadNetwork 路段）在每段路的中段生成一队守卫，
 * 宏伟度由道路宽度推算，守卫类型按风格轮换。
 * <p>
 * 由 Workmod.onEndTick 驱动；每 TICK_INTERVAL tick 处理一批路段。
 */
public final class GuardAutoSpawner {

    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");

    /** 每多少 tick 扫描一次路段（400 tick = 20 秒） */
    private static final int TICK_INTERVAL = 400;
    /** 每批最多处理的路段数 */
    private static final int MAX_PER_TICK = 4;

    /** 已生成守卫的路段 id */
    private static final Set<String> PROCESSED = new HashSet<>();
    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;

    private GuardAutoSpawner() {
    }

    /** 服务端每 tick 调用一次（由 Workmod 注册） */
    public static void tick(MinecraftServer server) {
        if (++tickCounter % TICK_INTERVAL != 0) {
            return;
        }
        ServerWorld world = server.getOverworld();
        if (world == null) {
            return;
        }
        int handled = 0;
        for (RoadSegment seg : RoadNetwork.getInstance().getAllSegments()) {
            if (handled >= MAX_PER_TICK) {
                break;
            }
            if (PROCESSED.contains(seg.getId())) {
                continue;
            }
            List<BlockPos> pts = seg.getPoints();
            if (pts.size() < 2) {
                PROCESSED.add(seg.getId());
                continue;
            }
            PROCESSED.add(seg.getId());
            handled++;

            BlockPos mid = pts.get(pts.size() / 2);
            int mag = Math.min(10, Math.max(1, seg.getStartWidth() / 2));
            GuardMonsterType type = GuardMonsterType.values()[RANDOM.nextInt(GuardMonsterType.values().length)];
            spawnSafe(world, mid, mag, type);
        }
    }

    /** 世界重载时重置处理记录 */
    public static void resetTracking() {
        PROCESSED.clear();
    }

    private static void spawnSafe(ServerWorld world, BlockPos mid, int mag, GuardMonsterType type) {
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, mid.getX(), mid.getZ());
        BlockPos ground = new BlockPos(mid.getX(), y, mid.getZ());
        try {
            MonsterSpawnManager.spawnGuards(world, ground, mag, type);
            LOGGER.info("[Workmod] 守卫已生成: {} @ {} (宏伟度 {})", type.getDisplayName(), ground, mag);
        } catch (Exception e) {
            LOGGER.warn("[Workmod] 守卫生成失败: {}", e.getMessage());
        }
    }
}
