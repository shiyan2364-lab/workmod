package com.shiyan2364.workmod.world.dimension;

import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 裂隙传送处理器
 * 每tick检测玩家是否站到裂隙门内传送方块（末地传送门框架）
 * 是 → 传送到目标维度建筑旁；归途门 → 传回主世界上次进入点
 */
public class RiftTeleportHandler {
    // 记录玩家上次从哪个主世界坐标进入其他维度（用于归途）
    private static final Map<UUID, BlockPos> LAST_OVERWORLD_POS = new HashMap<>();

    /**
     * 每tick调用（由Workmod的ServerTick注册）
     */
    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerWorld world = player.getServerWorld();
            BlockPos pos = player.getBlockPos();

            // 只在主世界/地狱/末地的门框正下方检测（脚下是传送方块）
            if (!world.getBlockState(pos).isOf(Blocks.END_PORTAL_FRAME)) {
                continue;
            }

            // 判断这是"主世界出发门"还是"归途门"
            if (world.getRegistryKey().equals(RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld")))) {
                // 主世界 → 找附近最近的裂隙传送点
                RiftPortal nearest = findNearestPortal(server, pos);
                if (nearest == null) continue;

                // 记录归途坐标（稍高于地板）
                LAST_OVERWORLD_POS.put(player.getUuid(), pos.up(2));

                ServerWorld targetWorld = server.getWorld(
                        RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", nearest.getTargetRealm().getDimensionId())));
                if (targetWorld == null) continue;

                BlockPos targetPos = nearest.getTargetPos();
                player.teleport(targetWorld, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5,
                        player.getYaw(), player.getPitch());
                player.sendMessage(net.minecraft.text.Text.literal(
                        "§d裂隙穿梭 → §f" + nearest.getTargetRealm().getDisplayName() + " §7(" + nearest.getLinkedStructureName() + ")"), false);
            } else {
                // 地狱/末地 → 归途门（传回主世界）
                BlockPos returnPos = LAST_OVERWORLD_POS.getOrDefault(player.getUuid(), new BlockPos(0, 100, 0));
                ServerWorld overworld = server.getOverworld();
                if (overworld == null) continue;

                player.teleport(overworld, returnPos.getX() + 0.5, returnPos.getY(), returnPos.getZ() + 0.5,
                        player.getYaw(), player.getPitch());
                player.sendMessage(net.minecraft.text.Text.literal("§b归途裂隙 → §f主世界"), false);
            }
        }
    }

    /**
     * 找距离最近的主世界裂隙（供主世界出发用）
     */
    private static RiftPortal findNearestPortal(MinecraftServer server, BlockPos pos) {
        RiftPortal nearest = null;
        double bestDist = Double.MAX_VALUE;
        for (RiftPortal portal : RiftManager.getAllPortals()) {
            double d = portal.getOverworldPos().getSquaredDistance(pos.getX(), pos.getY(), pos.getZ());
            if (d < bestDist) {
                bestDist = d;
                nearest = portal;
            }
        }
        return nearest;
    }

    public static void onPlayerDisconnect(UUID uuid) {
        LAST_OVERWORLD_POS.remove(uuid);
    }
}
