package com.shiyan2364.workmod.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Workmod 客户端入口
 * <p>
 * 注册：
 * - 高空云层渲染（云层高度优化：原版云不动，模组加多层高空云）
 * - 右上角小地图 HUD（方块颜色 + 结构标记 + 玩家位置）
 */
public class WorkmodClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HighCloudRenderer.register();
        MinimapHud.register();
    }
}
