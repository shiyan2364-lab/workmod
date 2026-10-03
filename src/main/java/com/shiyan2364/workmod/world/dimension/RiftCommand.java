package com.shiyan2364.workmod.world.dimension;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * 裂隙测试命令
 * /workmod:rift <overworld|nether|end>
 * 在玩家面前放一个出发门，并注册到 RiftManager
 */
public class RiftCommand {

    public static void register() {
        CommandRegistrationCallbackShim.register();
    }

    // 避免直接依赖内部类，抽一个shim
    private static class CommandRegistrationCallbackShim {
        static void register() {
            net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                dispatcher.register(CommandManager.literal("workmod")
                    .then(CommandManager.literal("rift")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.literal("overworld").executes(ctx -> placeRift(ctx, WorldRealm.OVERWORLD)))
                        .then(CommandManager.literal("nether").executes(ctx -> placeRift(ctx, WorldRealm.NETHER)))
                        .then(CommandManager.literal("end").executes(ctx -> placeRift(ctx, WorldRealm.END)))));
            });
        }
    }

    private static int placeRift(CommandContext<ServerCommandSource> ctx, WorldRealm targetRealm) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrThrow();
        ServerWorld world = ctx.getSource().getWorld();

        // 玩家面前 3 格放门
        Vec3d eye = player.getEyePos();
        Vec3d look = player.getRotationVector();
        BlockPos portalPos = new BlockPos(
                (int) Math.floor(eye.x + look.x * 3),
                (int) Math.floor(eye.y),
                (int) Math.floor(eye.z + look.z * 3)
        ).up(0);

        // 目标坐标：目标维度的大概对应位置（先用手工坐标，后续由结构生成时替换）
        BlockPos targetPos = switch (targetRealm) {
            case NETHER -> new BlockPos(portalPos.getX() / 8, 80, portalPos.getZ() / 8);
            case END -> new BlockPos(portalPos.getX(), 80, portalPos.getZ());
            default -> new BlockPos(0, 100, 0);
        };

        // 注册裂隙点
        RiftManager.registerPortal(new RiftPortal(
                portalPos,
                targetRealm,
                targetPos,
                "测试裂隙"
        ));

        // 放门
        RiftStructurePlacer.placeRift(world, portalPos, false);
        // 在目标维度放归途门（如果目标世界存在）
        ServerWorld targetWorld = ctx.getSource().getServer().getWorld(
                net.minecraft.registry.RegistryKey.of(
                        net.minecraft.registry.RegistryKeys.WORLD,
                        net.minecraft.util.Identifier.of("minecraft", targetRealm.getDimensionId())
                )
        );
        if (targetWorld != null && !targetWorld.isClient()) {
            RiftStructurePlacer.placeRift(targetWorld, targetPos, true);
        }

        ctx.getSource().sendFeedback(() -> Text.literal(
                "§a裂隙已生成！走到门内传送方块上进入 §e" + targetRealm.getDisplayName()), false);
        return 1;
    }
}
