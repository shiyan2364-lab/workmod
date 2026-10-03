package com.shiyan2364.workmod.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.shiyan2364.workmod.data.DifficultySavedData;
import com.shiyan2364.workmod.world.monsters.RealTimeDifficulty;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * OP 命令系统
 * /workmod:tp / list / info / reload / debug / anguish / boss_spawn ...
 */
public class WorkmodCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("workmod")
                .then(helpCommand())
                .then(tpCommand())
                .then(anguishCommand())
                .then(debugCommand())
            );
        });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> helpCommand() {
        return literal("help")
            .executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal("§6=== Workmod 命令 ==="), false);
                ctx.getSource().sendFeedback(() -> Text.literal("§e/workmod:tp <目标> §7传送（OP）"), false);
                ctx.getSource().sendFeedback(() -> Text.literal("§e/workmod:anguish §7实时难度"), false);
                ctx.getSource().sendFeedback(() -> Text.literal("§e/workmod:debug §7调试"), false);
                return 1;
            });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> tpCommand() {
        return literal("tp")
            .requires(src -> src.hasPermissionLevel(2)) // OP
            .executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal("§6用法: /workmod:tp <nearest_canyon|nearest_mountain|nearest_treasure|structure <id>>"), false);
                return 1;
            })
            .then(literal("nearest_canyon").executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal("§e传送系统待接入实际坐标"), false);
                return 1;
            }))
            .then(literal("nearest_mountain").executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal("§e传送系统待接入实际坐标"), false);
                return 1;
            }))
            .then(literal("nearest_treasure").executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal("§e传送系统待接入实际坐标"), false);
                return 1;
            }));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> anguishCommand() {
        return literal("anguish")
            .requires(src -> src.hasPermissionLevel(2))
            .executes(ctx -> {
                double small = RealTimeDifficulty.getInstance().getSmallEnergy();
                double large = RealTimeDifficulty.getInstance().getLargeEnergy();
                double bonus = RealTimeDifficulty.getInstance().getTotalBonus();
                ctx.getSource().sendFeedback(() -> Text.literal(
                    String.format("§6小怪能量: §f%.1f §6/ 大怪能量: §f%.1f §6/ 加成: §f%+.1f%%", small, large, bonus * 100)
                ), false);
                return 1;
            })
            .then(literal("reset").executes(ctx -> {
                RealTimeDifficulty.getInstance().reset();
                ctx.getSource().sendFeedback(() -> Text.literal("§a实时难度已重置"), false);
                return 1;
            }));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> debugCommand() {
        return literal("debug")
            .requires(src -> src.hasPermissionLevel(2))
            .executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal("§eWorkmod Debug: Phase 11"), false);
                ctx.getSource().sendFeedback(() -> Text.literal("§7- 道路: 已实现"), false);
                ctx.getSource().sendFeedback(() -> Text.literal("§7- 断路: 已实现"), false);
                ctx.getSource().sendFeedback(() -> Text.literal("§7- 峡谷/高山: 已实现"), false);
                ctx.getSource().sendFeedback(() -> Text.literal("§7- 补给/宝藏: 已实现"), false);
                ctx.getSource().sendFeedback(() -> Text.literal("§7- 怪物/难度: 已实现"), false);
                ctx.getSource().sendFeedback(() -> Text.literal("§7- NPC/任务: 已实现"), false);
                ctx.getSource().sendFeedback(() -> Text.literal("§7- 诅咒饰品: 已实现"), false);
                ctx.getSource().sendFeedback(() -> Text.literal("§7- 小地图: 已实现(标记)"), false);
                return 1;
            });
    }
}
