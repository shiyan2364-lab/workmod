package com.shiyan2364.workmod.world.road;

import com.shiyan2364.workmod.api.AdventureStructure;
import com.shiyan2364.workmod.api.StructureRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 道路自动生成驱动（阶段1 新增）
 * <p>
 * 挂在服务端 tick 上（Workmod.onEndTick 调用）：
 * 每隔 TICK_INTERVAL tick 处理一个注册结构，以在线玩家为中心、
 * SCAN_RADIUS 半径定位该结构的最近实例，找到后调用
 * {@link RoadGenerator#generateForStructure} 自动铺路。
 * <p>
 * 已处理记录带玩家所在区块坐标，玩家换区域后会重新尝试，
 * 避免一次性全图扫描卡服。
 */
public final class RoadAutoGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");

    /** 每多少 tick 处理一个结构（60 tick = 3 秒） */
    private static final int TICK_INTERVAL = 60;
    /** 结构定位半径 */
    private static final int SCAN_RADIUS = 256;

    /** 已生成道路的结构实例（key = 结构id@玩家区块坐标） */
    private static final Set<String> PROCESSED = new HashSet<>();
    /** 定位失败记录，避免同区域反复空扫 */
    private static final Set<String> FAILED = new HashSet<>();

    private static int tickCounter = 0;
    private static int cursor = 0;

    private RoadAutoGenerator() {
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
        List<AdventureStructure> all = new ArrayList<>(StructureRegistry.getAll());
        if (all.isEmpty()) {
            return;
        }
        ServerPlayerEntity player = firstPlayer(server);
        if (player == null) {
            return; // 无玩家在线，不扫描
        }
        BlockPos center = player.getBlockPos();

        // 轮询下一个结构，分摊负载
        AdventureStructure structure = all.get(cursor % all.size());
        cursor++;

        String key = structure.id().toString() + "@" + center.getX() / 16 + "," + center.getZ() / 16;
        if (PROCESSED.contains(key) || FAILED.contains(key)) {
            return;
        }

        BlockPos found = locate(world, structure, center, SCAN_RADIUS);
        if (found == null) {
            FAILED.add(key);
            return;
        }
        PROCESSED.add(key);
        RoadGenerator.generateForStructure(world, found, structure);
    }

    /** 世界重载时重置处理记录 */
    public static void reset() {
        PROCESSED.clear();
        FAILED.clear();
        cursor = 0;
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    private static ServerPlayerEntity firstPlayer(MinecraftServer server) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        return players.isEmpty() ? null : players.get(0);
    }

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
            LOGGER.warn("[Workmod] 自动定位结构 {} 失败: {}", structure.id(), e.getMessage());
            return null;
        }
    }
}
