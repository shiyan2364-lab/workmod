package com.shiyan2364.workmod.world.road;

/**
 * 道路宽度计算器
 * 根据宏伟度计算道路宽度（2格~100格）
 * 宏1=12 / 宏5=42 / 宏10=100
 */
public class RoadWidthCalculator {

    private static final int[] MAIN_WIDTHS = {12, 18, 24, 34, 42, 52, 64, 78, 90, 100};
    private static final int[] BRANCH_WIDTHS = {4, 6, 9, 12, 15, 18, 21, 25, 28, 32};
    private static final int[] PATH_WIDTHS = {2, 3, 4, 5, 6, 8, 9, 10, 12, 14};

    public static int mainRoadWidth(int magnificence) {
        if (magnificence <= 1) return MAIN_WIDTHS[0];
        if (magnificence >= 10) return MAIN_WIDTHS[9];
        return MAIN_WIDTHS[magnificence - 1];
    }

    public static int branchRoadWidth(int magnificence) {
        if (magnificence <= 1) return BRANCH_WIDTHS[0];
        if (magnificence >= 10) return BRANCH_WIDTHS[9];
        return BRANCH_WIDTHS[magnificence - 1];
    }

    public static int pathWidth(int magnificence) {
        if (magnificence <= 1) return PATH_WIDTHS[0];
        if (magnificence >= 10) return PATH_WIDTHS[9];
        return PATH_WIDTHS[magnificence - 1];
    }

    public static int getWidth(RoadType type, int magnificence) {
        return switch (type) {
            case MAIN -> mainRoadWidth(magnificence);
            case BRANCH -> branchRoadWidth(magnificence);
            case PATH -> pathWidth(magnificence);
        };
    }

    /**
     * 渐变宽度：从最大宽度向最小宽度线性过渡
     */
    public static int gradientWidth(int maxWidth, int minWidth, int distance, int fadeDistance) {
        if (distance <= 0) return maxWidth;
        if (distance >= fadeDistance) return minWidth;
        double t = (double) distance / fadeDistance;
        return (int) Math.round(maxWidth + (minWidth - maxWidth) * t);
    }
}
