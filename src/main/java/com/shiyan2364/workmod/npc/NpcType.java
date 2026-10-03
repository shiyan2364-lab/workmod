package com.shiyan2364.workmod.npc;

/**
 * NPC类型（8族，按建筑风格）
 * 每个NPC有专属交易/任务/声望
 */
public enum NpcType {
    ELDER("贤者长老", "村庄", "木"),
    WARLORD("战团长", "堡垒", "石"),
    SEER("先知", "遗迹", "时空"),
    TIDE_GUARDIAN("潮汐守护者", "海底", "海"),
    SNOW_HERMIT("雪隐士", "雪屋", "冰"),
    MOUNTAIN_SAGE("山地贤者", "高山", "大地"),
    FIRE_LORD("熔火领主", "下界", "焰"),
    VOID_WANDERER("虚空漫步者", "末地", "虚空");

    private final String displayName;
    private final String location;
    private final String element;

    NpcType(String displayName, String location, String element) {
        this.displayName = displayName;
        this.location = location;
        this.element = element;
    }

    public String getDisplayName() { return displayName; }
    public String getLocation() { return location; }
    public String getElement() { return element; }
}
