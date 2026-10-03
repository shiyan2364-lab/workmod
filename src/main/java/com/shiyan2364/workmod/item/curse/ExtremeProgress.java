package com.shiyan2364.workmod.item.curse;

import java.util.HashMap;
import java.util.Map;

/**
 * 极致进度记录
 * 记录每个系列的完成次数（Ⅰ/Ⅱ/Ⅲ）
 */
public class ExtremeProgress {
    private static final Map<ExtremeSeries, Integer> PROGRESS = new HashMap<>();

    static {
        for (ExtremeSeries series : ExtremeSeries.values()) {
            PROGRESS.put(series, 0);
        }
    }

    /**
     * 增加系列完成次数
     */
    public static void addProgress(ExtremeSeries series) {
        PROGRESS.put(series, PROGRESS.get(series) + 1);
    }

    /**
     * 获取系列完成次数
     */
    public static int getProgress(ExtremeSeries series) {
        return PROGRESS.get(series);
    }

    /**
     * 获取阶段：0=无 / 1=Ⅰ(>=1) / 2=Ⅱ(>=8) / 3=Ⅲ(>=64)
     */
    public static int getStage(ExtremeSeries series) {
        int count = PROGRESS.get(series);
        if (count >= 64) return 3;
        if (count >= 8) return 2;
        if (count >= 1) return 1;
        return 0;
    }

    /**
     * 获取默认强度（自动跟随极致进度）
     * 无=-100% / Ⅰ=-50% / Ⅱ=+100% / Ⅲ=+900%
     */
    public static int getDefaultStrength(ExtremeSeries series) {
        int stage = getStage(series);
        return switch (stage) {
            case 0 -> -100;
            case 1 -> -50;
            case 2 -> 100;
            case 3 -> 900;
            default -> -100;
        };
    }

    public static void reset() {
        for (ExtremeSeries series : ExtremeSeries.values()) {
            PROGRESS.put(series, 0);
        }
    }
}
