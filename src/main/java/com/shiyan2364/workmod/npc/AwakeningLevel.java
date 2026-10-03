package com.shiyan2364.workmod.npc;

/**
 * 觉醒等级（NPC + 建筑共用）
 * Lv0 沉睡 -> Lv4 主宰
 * 等级决定NPC可交易的特殊道具和建筑觉醒效果
 */
public enum AwakeningLevel {
    SLEEPING("沉睡", 0),
    GLIMMER("微光", 1),
    AWAKENED("觉醒", 2),
    BRILLIANT("辉煌", 3),
    DOMINANT("主宰", 4);

    private final String displayName;
    private final int level;

    AwakeningLevel(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() { return displayName; }
    public int getLevel() { return level; }

    public static AwakeningLevel fromLevel(int level) {
        return switch (level) {
            case 1 -> GLIMMER;
            case 2 -> AWAKENED;
            case 3 -> BRILLIANT;
            case 4 -> DOMINANT;
            default -> SLEEPING;
        };
    }

    public boolean canAccess(int requiredLevel) {
        return this.level >= requiredLevel;
    }
}
