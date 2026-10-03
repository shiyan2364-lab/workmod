package com.shiyan2364.workmod.api;

/**
 * 建筑规模枚举
 * 决定道路起始宽度与渐变范围
 */
public enum StructureScale {
    TINY(3, 1),
    SMALL(5, 1),
    MEDIUM(7, 2),
    LARGE(9, 3);

    private final int startWidth;
    private final int minWidth;

    StructureScale(int startWidth, int minWidth) {
        this.startWidth = startWidth;
        this.minWidth = minWidth;
    }

    public int getStartWidth() {
        return startWidth;
    }

    public int getMinWidth() {
        return minWidth;
    }
}
