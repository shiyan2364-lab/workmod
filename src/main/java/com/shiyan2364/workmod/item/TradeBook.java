package com.shiyan2364.workmod.item;

import com.shiyan2364.workmod.npc.NpcTradeEntry;
import com.shiyan2364.workmod.npc.NpcType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.List;
import java.util.Map;

/**
 * 旅者商册（线上 NPC 交易，无实体）
 * <p>
 * 用原版成书 NBT 生成：逐族列出 8 个 NPC 的交易（物品 / 觉醒等级 / 价格），
 * 出生时随手册一起发放。购买用命令 /workmod:buy（见 TradeCommand）。
 */
public final class TradeBook {

    public static final String TITLE = "旅者商册";

    private TradeBook() {
    }

    public static ItemStack create() {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putString("title", TITLE);
        nbt.putString("author", "古道商会");

        NbtList pages = new NbtList();
        for (Map.Entry<NpcType, List<NpcTradeEntry>> entry : TradeTable.TRADES.entrySet()) {
            NpcType npc = entry.getKey();
            StringBuilder page = new StringBuilder();
            page.append("{\"text\":\"§6§l").append(npc.getDisplayName())
                    .append(" §7(").append(npc.getLocation()).append(")\\n\\n\"}");
            for (NpcTradeEntry trade : entry.getValue()) {
                page.append("{\"text\":\"\\n§f").append(trade.getItemName())
                        .append("\\n§7觉醒≥").append(trade.getRequiredLevel())
                        .append(" §e").append(trade.getPrice()).append(" 绿宝石\\n")
                        .append("§8").append(trade.getDescription()).append("\"}");
            }
            pages.add(NbtString.of(page.toString()));
        }
        nbt.put("pages", pages);
        return stack;
    }

    public static boolean isTradeBook(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.isOf(Items.WRITTEN_BOOK)) {
            return false;
        }
        NbtCompound nbt = stack.getNbt();
        return nbt != null && TITLE.equals(nbt.getString("title"));
    }
}
