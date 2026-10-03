package com.shiyan2364.workmod.item;

import com.shiyan2364.workmod.npc.NpcTradeEntry;
import com.shiyan2364.workmod.npc.NpcType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 线上 NPC 交易表（无实体 NPC，用书 + 命令交易）
 * <p>
 * 每个 NPC 族 3 个交易项（物品名 / 觉醒等级需求 / 绿宝石价格 / 描述），
 * 价格便宜、受觉醒等级限制（NpcManager.canAccessTrade），
 * 对应说明书 6️⃣"NPC 觉醒等级限制交易、价格便宜"。
 */
public final class TradeTable {

    /** 8 族 NPC 的交易列表（顺序与 NpcType.values() 一致） */
    public static final Map<NpcType, List<NpcTradeEntry>> TRADES = buildTrades();

    /** 物品名 → 实际物品 */
    private static final Map<String, Item> ITEM_MAP = new HashMap<>();

    static {
        ITEM_MAP.put("铁锭", Items.IRON_INGOT);
        ITEM_MAP.put("钻石", Items.DIAMOND);
        ITEM_MAP.put("烈焰棒", Items.BLAZE_ROD);
        ITEM_MAP.put("下界合金碎片", Items.NETHERITE_SCRAP);
        ITEM_MAP.put("面包", Items.BREAD);
        ITEM_MAP.put("金苹果", Items.GOLDEN_APPLE);
        ITEM_MAP.put("生鳕鱼", Items.COD);
        ITEM_MAP.put("海晶碎片", Items.PRISMARINE_SHARD);
        ITEM_MAP.put("羊毛", Items.WHITE_WOOL);
        ITEM_MAP.put("雪球", Items.SNOWBALL);
        ITEM_MAP.put("末影珍珠", Items.ENDER_PEARL);
        ITEM_MAP.put("紫颂果", Items.CHORUS_FRUIT);
        ITEM_MAP.put("附魔之瓶", Items.EXPERIENCE_BOTTLE);
        ITEM_MAP.put("金锭", Items.GOLD_INGOT);
    }

    private TradeTable() {
    }

    private static Map<NpcType, List<NpcTradeEntry>> buildTrades() {
        Map<NpcType, List<NpcTradeEntry>> map = new HashMap<>();
        NpcType[] npcs = NpcType.values();
        String[][] data = {
                // ELDER 贤者长老
                {"金锭", "4", "15", "长者珍藏的金锭"},
                {"钻石", "6", "24", "觉醒者的报酬"},
                {"附魔之瓶", "2", "6", "灌注了远古知识的瓶"},
                // FIRE_LORD 熔火领主
                {"烈焰棒", "2", "5", "熔岩中的信标"},
                {"下界合金碎片", "4", "12", "烬土的精髓"},
                {"面包", "1", "2", "旅途干粮"},
                // MOUNTAIN_SAGE 山地贤者
                {"面包", "1", "2", "高山下的粮仓"},
                {"金苹果", "3", "8", "登山者的奖赏"},
                {"羊毛", "1", "3", "雪原的馈赠"},
                // SEER 先知
                {"附魔之瓶", "2", "6", "窥见命运的一角"},
                {"末影珍珠", "3", "10", "星辰的碎片"},
                {"钻石", "6", "24", "先知之眼"},
                // SNOW_HERMIT 雪隐士
                {"雪球", "1", "1", "冻原的玩具"},
                {"羊毛", "1", "3", "御寒的毛皮"},
                {"面包", "1", "2", "隐居者的口粮"},
                // TIDE_GUARDIAN 潮汐守护者
                {"生鳕鱼", "1", "2", "潮汐的赐予"},
                {"海晶碎片", "3", "6", "深海的辉光"},
                {"钻石", "6", "24", "海渊之心"},
                // VOID_WANDERER 虚空漫步者
                {"末影珍珠", "3", "10", "虚空的门票"},
                {"紫颂果", "2", "5", "界外之果"},
                {"金锭", "4", "15", "流浪者的积蓄"},
                // WARLORD 战团长
                {"铁锭", "2", "5", "征战的余料"},
                {"钻石", "4", "12", "胜者的酬金"},
                {"金苹果", "3", "8", "沙场保命符"},
        };
        int idx = 0;
        for (NpcType npc : npcs) {
            List<NpcTradeEntry> list = new ArrayList<>();
            for (int t = 0; t < 3; t++, idx++) {
                String[] row = data[idx];
                list.add(new NpcTradeEntry(row[0], Integer.parseInt(row[1]), row[2], row[3]));
            }
            map.put(npc, list);
        }
        return map;
    }

    /** 物品名 → 实际物品（找不到返回 null） */
    public static Item resolveItem(String itemName) {
        return ITEM_MAP.get(itemName);
    }

    /** 价格解析（价格字段为数字字符串） */
    public static int priceOf(NpcTradeEntry entry) {
        try {
            return Math.max(1, Integer.parseInt(entry.getPrice()));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
