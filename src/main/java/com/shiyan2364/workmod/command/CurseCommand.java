package com.shiyan2364.workmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.shiyan2364.workmod.item.curse.CurseItem;
import com.shiyan2364.workmod.item.curse.CurseManager;
import com.shiyan2364.workmod.item.curse.CurseItem.CurseType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * 诅咒饰品强度调节命令（说明书 6️⃣：可手动调节强度）
 * <p>
 * /workmod:curse list —— 查看 8 件饰品当前强度与模式（自动/手动）
 * /workmod:curse set <id 0-7> <强度> —— 手动设置强度
 * /workmod:curse reset <id 0-7> —— 恢复默认（重新跟随极致进度）
 * /workmod:curse resetall —— 全部恢复默认
 * <p>
 * 区间约束（反转前/反转后）：
 * - 未反转（纯诅咒期）：只能设负强度 -100 ~ 0；
 * - 已反转（集齐圣器）：可设 -100 ~ +900。
 */
public final class CurseCommand {

    private CurseCommand() {
    }

    /** 由 Workmod.onInitialize 调用 */
    public static void register() {
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
                CurseCommand::onRegister);
    }

    private static void onRegister(CommandDispatcher<ServerCommandSource> dispatcher,
                                   CommandRegistryAccess registryAccess,
                                   CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("workmod")
                .then(literal("curse")
                        .then(literal("list").executes(ctx -> list(ctx.getSource())))
                        .then(literal("set")
                                .then(argument("id", IntegerArgumentType.integer(0, 7))
                                        .then(argument("strength", IntegerArgumentType.integer(-100, 900))
                                                .executes(ctx -> set(ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "id"),
                                                        IntegerArgumentType.getInteger(ctx, "strength"))))))
                        .then(literal("reset")
                                .then(argument("id", IntegerArgumentType.integer(0, 7))
                                        .executes(ctx -> reset(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "id")))))
                        .then(literal("resetall").executes(ctx -> resetAll(ctx.getSource())))));
    }

    private static int list(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("§c此命令仅玩家可用"));
            return 0;
        }
        CurseType[] types = CurseType.values();
        boolean awakened = CurseManager.isFullAwakened();
        StringBuilder sb = new StringBuilder();
        sb.append("§6=== 诅咒饰品 ===\n");
        sb.append(awakened ? "§a已反转：可设正强度（-100 ~ +900）\n"
                : "§7未反转：仅可设负强度（-100 ~ 0）\n");
        for (int i = 0; i < types.length; i++) {
            CurseType t = types[i];
            int strength = CurseItem.getStrength(t);
            boolean manual = CurseItem.isManualMode(t);
            sb.append("§f[").append(i).append("] ").append(t.getDisplayName())
                    .append(" §7强度: §").append(strength >= 0 ? "e+" : "c")
                    .append(strength).append("% §8[")
                    .append(manual ? "手动" : "自动").append("]\n");
        }
        sb.append("§7/workmod:curse set <id> <强度> | reset <id> | resetall");
        player.sendMessage(Text.literal(sb.toString()), false);
        return 1;
    }

    private static int set(ServerCommandSource source, int id, int strength) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        CurseType type = CurseType.values()[id];
        boolean awakened = CurseManager.isFullAwakened();
        int max = awakened ? 900 : 0;
        if (strength > max) {
            source.sendError(Text.literal("§c强度超出允许区间（" + (awakened ? "-100 ~ +900" : "-100 ~ 0 未反转") + "）"));
            return 0;
        }
        CurseItem.setCustomStrength(type, strength);
        source.sendFeedback(() -> Text.literal("§a已手动设置「" + type.getDisplayName()
                + "」强度 " + strength + "%（" + (CurseItem.isManualMode(type) ? "手动" : "自动") + "）"), false);
        return 1;
    }

    private static int reset(ServerCommandSource source, int id) {
        CurseType type = CurseType.values()[id];
        CurseItem.resetToDefault(type);
        source.sendFeedback(() -> Text.literal("§a「" + type.getDisplayName()
                + "」已恢复默认（跟随极致进度）"), false);
        return 1;
    }

    private static int resetAll(ServerCommandSource source) {
        CurseItem.resetAll();
        source.sendFeedback(() -> Text.literal("§a全部诅咒饰品已恢复默认（跟随极致进度）"), false);
        return 1;
    }
}
