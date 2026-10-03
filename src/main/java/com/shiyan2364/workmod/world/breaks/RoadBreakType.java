package com.shiyan2364.workmod.world.breaks;

/**
 * 断路类型枚举
 * CLIFF=悬崖断裂（无法直接通过）
 * RUIN=遗迹废墟（可挖开，有奖励箱）
 * BRIDGE=河流断桥（熄灭篝火做桥面，可步行）
 * VINES=藤蔓封路（可砍掉，掉食物）
 */
public enum RoadBreakType {
    CLIFF("悬崖", false),
    RUIN("遗迹", true),
    BRIDGE("篝火桥", false),
    VINES("藤蔓", true);

    private final String displayName;
    private final boolean hasReward;

    RoadBreakType(String displayName, boolean hasReward) {
        this.displayName = displayName;
        this.hasReward = hasReward;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean hasReward() {
        return hasReward;
    }
}
