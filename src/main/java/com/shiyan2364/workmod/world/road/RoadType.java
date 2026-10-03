package com.shiyan2364.workmod.world.road;

/**
 * 道路类型枚举
 * MAIN=主路（宽，连接宏伟建筑）
 * BRANCH=支线（中，连接次级建筑/汇入主路）
 * PATH=小径（窄，连接残迹/废弃点）
 */
public enum RoadType {
    MAIN("主路"),
    BRANCH("支线"),
    PATH("小径");

    private final String displayName;

    RoadType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
