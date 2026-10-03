package com.shiyan2364.workmod.world.monsters;

/**
 * 守卫怪物类型（8类，按建筑风格）
 */
public enum GuardMonsterType {
    CORRUPTED_VILLAGER("腐化村民", "村庄", 1),
    IRON_RAIDER("铁甲掠夺者", "堡垒", 2),
    WRAITH_GUARD("怨灵守卫", "遗迹", 3),
    DEEP_DIVER("深潜者", "海底", 4),
    FROST_TROLL("冰霜巨魔", "雪地", 5),
    STONE_GOLEM("石甲巨人", "山地", 6),
    LAVA_GOLEM("熔岩魔像", "下界", 7),
    VOID_WATCHER("虚空守望者", "末地", 8);

    private final String displayName;
    private final String location;
    private final int index;

    GuardMonsterType(String displayName, String location, int index) {
        this.displayName = displayName;
        this.location = location;
        this.index = index;
    }

    public String getDisplayName() { return displayName; }
    public String getLocation() { return location; }
    public int getIndex() { return index; }
}
