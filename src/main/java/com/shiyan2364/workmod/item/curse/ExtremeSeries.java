package com.shiyan2364.workmod.item.curse;

/**
 * 极致系列枚举（八大系列）
 * 每个系列对应一个诅咒饰品
 * Ⅰ=×1 / Ⅱ=×8 / Ⅲ=×64
 */
public enum ExtremeSeries {
    SKY("苍穹", "登顶高山", "傲慢之冠"),
    ABYSS("深渊", "潜入峡谷", "贪婪之手"),
    TIDE("沧浪", "海底探索", "恐惧之心"),
    ASH("烬土", "下界生存", "暴怒之锤"),
    STAR("星辉", "天空/末地", "嫉妒之眼"),
    TIME("时痕", "时间积累", "怠惰之石"),
    ROCK("岩魂", "地下挖掘", "恐岩之心"),
    LIFE("生机", "自然/生命", "色欲之花");

    private final String displayName;
    private final String description;
    private final String curseItemName;

    ExtremeSeries(String displayName, String description, String curseItemName) {
        this.displayName = displayName;
        this.description = description;
        this.curseItemName = curseItemName;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getCurseItemName() { return curseItemName; }
}
