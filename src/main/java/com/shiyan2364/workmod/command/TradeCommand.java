package com.shiyan2364.workmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.shiyan2364.workmod.item.TradeTable;
import com.shiyan2364.workmod.npc.NpcManager;
import com.shiyan2364.workmod.npc.NpcTradeEntry;
import com.shiyan2364.workmod.npc.NpcType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * 线上 NPC 交易命令（无实体 NPC）
 * <p>
 * /workmod:market —— 查看全部 8 族 NPC 交易
 * /workmod:market <npcId> —— 查看某一族
 * /workmod:buy <npcId> <tradeIndex> —— 用绿宝石购买（受觉醒等级限制）
 */
public final class TradeCommand {

    private TradeCommand() {
    }

    /** 由 Workmod.onInitialize 调用（往现有 /workmod 根节点挂子命令） */
    public static void register() {
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
                TradeCommand::onRegister);
    }

    private static void onRegister(CommandDispatcher<ServerCommandSource> dispatcher,
                                   CommandRegistryAccess registryAccess,
                                   CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("workmod")
                .then(literal("market")
                        .executes(ctx -> listAll(ctx.getSource()))
                        .then(argument("npc", IntegerArgumentType.integer(0, 7))
                                .executes(ctx -> listOne(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "npc")))))
                .then(literal("buy")
                        .then(argument("npc", IntegerArgumentType.integer(0, 7))
                                .then(argument("trade", IntegerArgumentType.integer(0, 2))
                                        .executes(ctx -> buy(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "npc"),
                                                IntegerArgumentType.getInteger(ctx, "trade")))))));
    }

    private static int listAll(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("§c此命令仅玩家可用"));
            return 0;
        }
        StringBuilder sb = new StringBuilder("§6=== 旅者商会 ===\n");
        NpcType[] npcs = NpcType.values();
        for (int i = 0; i < npcs.length; i++) {
            NpcType npc = npcs[i];
            sb.append("§e[").append(i).append("] ").append(npc.getDisplayName())
                    .append(" §7(").append(npc.getLocation()).append(") §8觉醒:")
                    .append(NpcManager.getAwakening(npc).getLevel()).append("\n");
        }
        sb.append("§7/workmod:market <id> 查看详情；/workmod:buy <id> <t> 购买");
        player.sendMessage(Text.literal(sb.toString()), false);
        return 1;
    }

    private static int listOne(ServerCommandSource source, int npcId) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        NpcType npc = NpcType.values()[npcId];
        List<NpcTradeEntry> trades = TradeTable.TRADES.get(npc);
        StringBuilder sb = new StringBuilder("§6=== ").append(npc.getDisplayName()).append(" ===\n");
        for (int t = 0; t < trades.size(); t++) {
            NpcTradeEntry trade = trades.get(t);
            boolean ok = NpcManager.canAccessTrade(npc, trade.getRequiredLevel());
            sb.append(ok ? "§a" : "§7").append("[").append(t).append("] ")
                    .append(trade.getItemName())
                    .append(" §e").append(TradeTable.priceOf(trade)).append(" 绿宝石")
                    .append(ok ? "" : " §c(觉醒不足)")
                    .append("\n");
        }
        player.sendMessage(Text.literal(sb.toString()), false);
        return 1;
    }

    private static int buy(ServerCommandSource source, int npcId, int tradeIndex) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("§c此命令仅玩家可用"));
            return 0;
        }
        NpcType npc = NpcType.values()[npcId];
        List<NpcTradeEntry> trades = TradeTable.TRADES.get(npc);
        if (tradeIndex < 0 || tradeIndex >= trades.size()) {
            source.sendError(Text.literal("§c交易编号无效"));
            return 0;
        }
        NpcTradeEntry trade = trades.get(tradeIndex);
        if (!NpcManager.canAccessTrade(npc, trade.getRequiredLevel())) {
            source.sendError(Text.literal("§c觉醒等级不足（需 ≥" + trade.getRequiredLevel() + "）"));
            return 0;
        }
        Item item = TradeTable.resolveItem(trade.getItemName());
        if (item == null) {
            source.sendError(Text.literal("§c该交易物品暂缺货"));
            return 0;
        }
        int price = TradeTable.priceOf(trade);
        if (player.getInventory().count(Items.EMERALD) < price) {
            source.sendError(Text.literal("§c绿宝石不足（需要 " + price + " 颗）"));
            return 0;
        }
        // 遍历背包扣除绿宝石（兼容 1.20.1 Inventory API）
        int remaining = price;
        for (ItemStack stack : player.getInventory().main) {
            if (stack.isOf(Items.EMERALD)) {
                int take = Math.min(stack.getCount(), remaining);
                stack.decrement(take);
                remaining -= take;
                if (remaining <= 0) {
                    break;
                }
            }
        }
        player.giveItemStack(new ItemStack(item));
        // 交易成功提升一点声望
        NpcManager.addReputation(npc, 1);
        source.sendFeedback(() -> Text.literal("§a交易成功：" + trade.getItemName()
                + "（" + npc.getDisplayName() + " 声望 +1）"), false);
        return 1;
    }
}
