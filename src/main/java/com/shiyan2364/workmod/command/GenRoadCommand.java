package com.shiyan2364.workmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.shiyan2364.workmod.api.StructureStyle;
import com.shiyan2364.workmod.world.road.RoadNetwork;
import com.shiyan2364.workmod.world.road.RoadType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * /workmod:genroad —— 测试铺路命令（阶段1）
 * <p>
 * 在玩家面前沿朝向铺一条 60 格、宽度渐变的测试路，验证铺路管线。
 * 自动铺路由 RoadAutoGenerator 驱动，本命令仅用于调试。
 * <p>
 * 注意：register() 必须是无参方法（与 WorkmodCommand 同款模式），
 * 由 Workmod.onInitialize 调用；命令本体在内部通过
 * CommandRegistrationCallback 注册。
 */
public final class GenRoadCommand {

    private static final int TEST_LENGTH = 60;
    private static final int TEST_START_WIDTH = 4;
    private static final int TEST_END_WIDTH = 2;

    private GenRoadCommand() {
    }

    /** 由 Workmod.onInitialize 调用 */
    public static void register() {
        CommandRegistrationCallback.EVENT.register(GenRoadCommand::onRegister);
    }

    private static void onRegister(CommandDispatcher<ServerCommandSource> dispatcher,
                                   CommandRegistryAccess registryAccess,
                                   CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("workmod:genroad")
                .executes(ctx -> execute(ctx.getSource())));
    }

    private static int execute(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("§c此命令仅玩家可用"));
            return 0;
        }
        ServerWorld world = player.getServerWorld();
        BlockPos base = player.getBlockPos();
        Direction facing = player.getHorizontalFacing();

        try {
            BlockPos start = base.offset(facing, 2);
            BlockPos end = base.offset(facing, TEST_LENGTH);
            RoadNetwork network = RoadNetwork.getInstance();
            network.placeRoad(world, RoadType.MAIN, start, end,
                    TEST_START_WIDTH, TEST_END_WIDTH, 12, StructureStyle.VILLAGE.name());
            source.sendFeedback(
                    () -> Text.literal("§a已生成测试路 " + TEST_LENGTH + " 格，当前路网共 "
                            + network.getAllSegments().size() + " 段"),
                    false);
        } catch (Exception e) {
            source.sendError(Text.literal("§c铺路失败: " + e.getMessage()));
        }
        return 1;
    }
}
