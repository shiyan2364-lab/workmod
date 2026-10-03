package com.shiyan2364.workmod.world.canyons;

import net.minecraft.util.math.BlockPos;

/**
 * 峡谷数据
 * 记录一条大峡谷的位置/尺寸/底部类型
 */
public class CanyonData {
    private final BlockPos center;      // 峡谷中心（地表）
    private final int width;            // 横向宽度（>=50格）
    private final int length;           // 纵向长度（>=60格）
    private final int depth;            // 深度（60~1800格，可控）
    private int bottomType;             // 0=普通, 1=岩浆, 2=虚空, 3=下界传送门
    private boolean hasHiddenPath;      // 是否有隐藏石阶
    private boolean discovered;         // 玩家是否发现

    public CanyonData(BlockPos center, int width, int length, int depth) {
        this.center = center;
        this.width = width;
        this.length = length;
        this.depth = depth;
        this.bottomType = 0;
        this.hasHiddenPath = false;
        this.discovered = false;
    }

    public BlockPos getCenter() { return center; }
    public int getWidth() { return width; }
    public int getLength() { return length; }
    public int getDepth() { return depth; }
    public int getBottomType() { return bottomType; }
    public boolean hasHiddenPath() { return hasHiddenPath; }
    public boolean isDiscovered() { return discovered; }

    public void setBottomType(int bottomType) { this.bottomType = bottomType; }
    public void setHiddenPath(boolean hasHiddenPath) { this.hasHiddenPath = hasHiddenPath; }
    public void setDiscovered(boolean discovered) { this.discovered = discovered; }

    /**
     * 校验面积是否满足设计（横×长≥3000）
     */
    public boolean isValid() {
        return width * length >= 3000;
    }
}
