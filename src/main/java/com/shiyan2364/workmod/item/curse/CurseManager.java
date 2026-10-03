package com.shiyan2364.workmod.item.curse;

/**
 * 诅咒饰品管理器
 * 封装：获取强度/调节强度/重置/全套检查
 */
public class CurseManager {

    /**
     * 计算全套加成（按所有饰品实际强度平均值）
     * 主戒+7伴生 = 8件
     */
    public static double getSetBonus() {
        double total = 0;
        int count = 0;

        for (CurseItem.CurseType type : CurseItem.CurseType.values()) {
            int strength = CurseItem.getStrength(type);
            // 只计算正强度（觉醒后）
            if (strength > 0) {
                total += strength;
            }
            count++;
        }

        if (count == 0) return 0;

        double avg = total / count;

        // 基础套件效果：按已反转件数
        int awakenedCount = 0;
        for (CurseItem.CurseType type : CurseItem.CurseType.values()) {
            if (CurseItem.getStrength(type) > 0) {
                awakenedCount++;
            }
        }

        double baseBonus = switch (awakenedCount) {
            case 1 -> 0.05;
            case 2 -> 0.10;
            case 3 -> 0.15;
            case 4 -> 0.20;
            case 5 -> 0.30;
            case 6 -> 0.40;
            case 7 -> 0.50;
            case 8 -> 0.80;
            default -> 0;
        };

        return baseBonus * (avg / 100.0);
    }

    /**
     * 检查是否全套觉醒
     */
    public static boolean isFullAwakened() {
        for (CurseItem.CurseType type : CurseItem.CurseType.values()) {
            if (CurseItem.getStrength(type) <= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取已觉醒件数
     */
    public static int getAwakenedCount() {
        int count = 0;
        for (CurseItem.CurseType type : CurseItem.CurseType.values()) {
            if (CurseItem.getStrength(type) > 0) {
                count++;
            }
        }
        return count;
    }

    public static void resetAll() {
        CurseItem.resetAll();
    }
}
