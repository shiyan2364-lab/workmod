package com.shiyan2364.workmod.api;

/**
 * 建筑风格枚举
 * 决定道路方块样式、补给点装饰、断路类型
 */
public enum StructureStyle {
    VILLAGE("村庄", "wood"),
    FORTRESS("堡垒", "stone"),
    TEMPLE("神庙", "sand"),
    RUIN("遗迹", "cracked"),
    OCEAN("海底", "prismarine"),
    SNOW("雪原", "snow"),
    MOUNTAIN("山地", "stone");

    private final String displayName;
    private final String roadTheme;

    StructureStyle(String displayName, String roadTheme) {
        this.displayName = displayName;
        this.roadTheme = roadTheme;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRoadTheme() {
        return roadTheme;
    }
}
