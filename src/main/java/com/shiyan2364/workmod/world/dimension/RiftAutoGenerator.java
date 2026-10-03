package com.shiyan2364.workmod.world.dimension;

import com.shiyan2364.workmod.world.road.RoadNetwork;
import com.shiyan2364.workmod.world.road.RoadSegment;
import com.shiyan2364.workmod.world.road.RoadType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 裂隙自动生成器
 * 扫描路网中的主路，在道路末端点自动生成立场裂隙门（地狱/末地随机）
 * 同时自动在目标维度放置归途门
 * 已处理的道路ID记录在本地Set，避免重复生成
 */
public class RiftAutoGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final Set<String> PROCESSED_ROADS = new HashSet<>();
    private static final Random RANDOM = new Random();

    // 只在服务器tick中调用
    public static void tick(MinecraftServer server) {
        if (server.getOverworld() == null) return;

        for (RoadSegment seg : RoadNetwork.getInstance().getAllSegments()) {
            // 只处理主路，且未处理
            if (seg.getType() != RoadType.MAIN || PROCESSED_ROADS.contains(seg.getId())) {
                continue;
            }

            // 道路长度足够才生成裂隙（防止小建筑间短路出裂隙）
            if (seg.getPoints().size() < 200) continue;

            // 取末端点（远离起点的一端）
            BlockPos end = seg.getPoints().get(seg.getPoints().size() - 1);
            ServerWorld overworld = server.getOverworld();

            // 随机目标维度：50% 地狱 / 50% 末地
            boolean toNether = RANDOM.nextBoolean();
            WorldRealm targetRealm = toNether ? WorldRealm.NETHER : WorldRealm.END;

            // 目标坐标自动换算
            BlockPos targetPos = switch (targetRealm) {
                case NETHER -> new BlockPos(end.getX() / 8, 100, end.getZ() / 8);
                case END -> new BlockPos(end.getX(), 100, end.getZ());
                default -> new BlockPos(end.getX(), 100, end.getZ());
            };

            // 检查是否已经有类似的裂隙（同末端附近20格）
            boolean alreadyExists = false;
            for (RiftPortal portal : RiftManager.getAllPortals()) {
                if (Math.abs(portal.getOverworldPos().getX() - end.getX()) <= 20 &&
                    Math.abs(portal.getOverworldPos().getZ() - end.getZ()) <= 20) {
                    alreadyExists = true;
                    break;
                }
            }
            if (alreadyExists) {
                PROCESSED_ROADS.add(seg.getId());
                continue;
            }

            // 生成主世界出发门
            if (overworld != null && !overworld.isClient()) {
                RiftStructurePlacer.placeRift(overworld, end.up(0), false);
                RiftManager.registerPortal(new RiftPortal(
                        end.up(0),
                        targetRealm,
                        targetPos,
                        "古道裂隙·" + (toNether ? "熔岩之门" : "星辉之门")
                ));
                LOGGER.info("[Workmod] 裂隙自动门 @ {} → {}", end, targetRealm.getDisplayName());
            }

            // 在目标维度放归途门
            ServerWorld targetWorld = server.getWorld(
                    RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", targetRealm.getDimensionId())));
            if (targetWorld != null && !targetWorld.isClient()) {
                RiftStructurePlacer.placeRift(targetWorld, targetPos, true);
                LOGGER.info("[Workmod] 归途门 @ {} ({})", targetPos, targetRealm.getDisplayName());
            }

            PROCESSED_ROADS.add(seg.getId());
        }
    }

    public static void resetTracking() {
        PROCESSED_ROADS.clear();
    }
}
