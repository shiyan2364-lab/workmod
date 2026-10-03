package com.shiyan2364.workmod.ui;

import net.minecraft.util.math.BlockPos;

/**
 * 小地图标记（用字母+颜色代码，不用emoji）
 */
public class MapMarker {
    public enum MarkerType {
        BUILDING("建筑", "§6B", "§6"),
        CANYON("峡谷", "§5C", "§5"),
        MOUNTAIN("高山", "§fM", "§f"),
        SUPPLY("补给", "§aS", "§a"),
        TREASURE("宝藏", "§eT", "§e"),
        BREAK("断路", "§cX", "§c"),
        STONE("石碑", "§bO", "§b");

        private final String displayName;
        private final String icon;
        private final String color;

        MarkerType(String displayName, String icon, String color) {
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public String getColor() { return color; }
    }

    private final BlockPos pos;
    private final MarkerType type;
    private final String label;

    public MapMarker(BlockPos pos, MarkerType type, String label) {
        this.pos = pos;
        this.type = type;
        this.label = label;
    }

    public BlockPos getPos() { return pos; }
    public MarkerType getType() { return type; }
    public String getLabel() { return label; }
}
