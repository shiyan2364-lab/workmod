package com.shiyan2364.workmod.npc;

/**
 * NPC交易条目
 * 每个交易对应一个觉醒等级限制
 * 价格便宜（符合冒险者没钱的设定）
 */
public class NpcTradeEntry {
    private final String itemName;
    private final int requiredLevel;
    private final String price;
    private final String description;

    public NpcTradeEntry(String itemName, int requiredLevel, String price, String description) {
        this.itemName = itemName;
        this.requiredLevel = requiredLevel;
        this.price = price;
        this.description = description;
    }

    public String getItemName() { return itemName; }
    public int getRequiredLevel() { return requiredLevel; }
    public String getPrice() { return price; }
    public String getDescription() { return description; }
}
