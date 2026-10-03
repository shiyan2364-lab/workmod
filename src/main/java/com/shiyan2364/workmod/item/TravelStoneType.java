package com.shiyan2364.workmod.item;

/**
 * 传送石碑类型（联动 Waystones）
 * 无名/旅者=5分钟CD+消耗珍珠；古老/魂系=无CD免费
 */
public enum TravelStoneType {
    UNNAMED("无名石碑", 6000, true),
    TRAVELER("旅者石碑", 6000, true),
    ANCIENT("古老石碑", 0, false),
    SOUL("魂系石碑", 0, false);

    private final String displayName;
    private final int cooldownTicks;
    private final boolean requiresPearl;

    TravelStoneType(String displayName, int cooldownTicks, boolean requiresPearl) {
        this.displayName = displayName;
        this.cooldownTicks = cooldownTicks;
        this.requiresPearl = requiresPearl;
    }

    public String getDisplayName() { return displayName; }
    public int getCooldownTicks() { return cooldownTicks; }
    public boolean requiresPearl() { return requiresPearl; }
}
