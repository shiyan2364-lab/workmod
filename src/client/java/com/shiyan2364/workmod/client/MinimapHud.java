package com.shiyan2364.workmod.client;

import com.shiyan2364.workmod.ui.MapManager;
import com.shiyan2364.workmod.ui.MapMarker;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * 右上角小地图 HUD（骨架实装）
 * <p>
 * 以玩家为中心渲染 64×64 像素小地图（每像素 = 3 格）：
 * - 方块颜色（从世界方块采样，用 MapColor 的渲染色）；
 * - 白点 = 玩家；
 * - 绿点 = MapManager 里标记的结构/路碑（由铺路系统 addMarker）。
 * <p>
 * 性能：每帧采 64×64≈4096 个方块，可接受；想关掉把 register() 那行删掉即可。
 */
public final class MinimapHud {

    /** 小地图像素尺寸 */
    private static final int SIZE = 64;
    /** 每像素代表的世界格数 */
    private static final int SCALE = 3;
    /** 半尺寸（格） */
    private static final int HALF = (SIZE / 2) * SCALE;
    /** 右上角边距 */
    private static final int MARGIN = 4;

    private MinimapHud() {
    }

    /** 由 WorkmodClient 调用注册 */
    public static void register() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            try {
                render(context);
            } catch (Exception ignored) {
            }
        });
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return;
        }
        BlockPos center = client.player.getBlockPos();
        int x0 = client.getWindow().getScaledWidth() - SIZE - MARGIN;
        int y0 = MARGIN;

        // 背景
        context.fill(x0 - 1, y0 - 1, x0 + SIZE + 1, y0 + SIZE + 1, 0xCC111111);
        context.fill(x0, y0, x0 + SIZE, y0 + SIZE, 0xAA223344);

        // 采样方块颜色
        World world = client.world;
        for (int px = 0; px < SIZE; px++) {
            for (int py = 0; py < SIZE; py++) {
                int wx = center.getX() - HALF + px * SCALE;
                int wz = center.getZ() - HALF + py * SCALE;
                BlockState state = world.getBlockState(new BlockPos(wx, center.getY(), wz));
                int color = blockColor(state);
                if (color != 0) {
                    context.fill(x0 + px, y0 + py, x0 + px + 1, y0 + py + 1, color);
                }
            }
        }

        // 结构/路标记点（绿点）
        List<MapMarker> markers = MapManager.getMarkersInRange(center, 256);
        for (MapMarker m : markers) {
            int mx = x0 + (center.getX() - m.getPos().getX()) / SCALE + SIZE / 2;
            int mz = y0 + (center.getZ() - m.getPos().getZ()) / SCALE + SIZE / 2;
            if (mx >= x0 && mx < x0 + SIZE && mz >= y0 && mz < y0 + SIZE) {
                context.fill(mx - 1, mz - 1, mx + 2, mz + 2, 0xFF00FF00);
            }
        }

        // 玩家位置（中心白点）
        context.fill(x0 + SIZE / 2 - 1, y0 + SIZE / 2 - 1, x0 + SIZE / 2 + 2, y0 + SIZE / 2 + 2, 0xFFFFFFFF);

        // 北向指示
        context.drawText(client.textRenderer, "N", x0 + SIZE / 2 - 3, y0 - 9, 0xFFFFFFFF, true);
    }

    /** 从方块取渲染色（转成不透明 ARGB，0 表示透明/空气） */
    private static int blockColor(BlockState state) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) {
                return 0;
            }
            MapColor mc = state.getMapColor(client.world, client.player.getBlockPos());
            int argb = mc.color;
            if (argb == 0) {
                return 0;
            }
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        } catch (Exception e) {
            return 0;
        }
    }
}
