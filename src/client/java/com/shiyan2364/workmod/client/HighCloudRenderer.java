package com.shiyan2364.workmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * 高空云层渲染器（云层高度优化）
 * <p>
 * 不 mixin、不改原版 WorldRenderer：通过 Fabric 渲染事件
 * （WorldRenderEvents.AFTER_TRANSLUCENT）在云层渲染完成后，
 * 按 {@link CloudConfig#EXTRA_HEIGHTS} 在多个高度自绘半透明"云海平面"。
 * <p>
 * - 原版云（Y≈128）完全不动；
 * - 高层云用原版云纹理（textures/environment/clouds.png），随时间缓慢漂移；
 * - 跟随"云：开启"选项（关云时一并隐藏）；
 * - 每层仅 1 个四边形，性能开销可忽略。
 */
public final class HighCloudRenderer {

    private static final Identifier CLOUDS_TEXTURE = new Identifier("textures/environment/clouds.png");

    /** 云纹理漂移累计值 */
    private static float drift = 0f;

    private HighCloudRenderer() {
    }

    /** 由 WorkmodClient 调用注册 */
    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            try {
                render(context);
            } catch (Exception ignored) {
                // 渲染异常不影响游戏，静默跳过
            }
        });
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }
        // 跟随原版"云"选项（FAST/FANCY 才显示；OFF 隐藏）
        // 1.20.1 无 SimpleOption.isCloudsEnabled()（1.20.2+ 才有），改为比较枚举值
        if (client.options == null || client.options.getCloudRenderMode().getValue() == CloudRenderMode.OFF) {
            return;
        }
        Camera camera = context.camera();
        if (camera == null) {
            return;
        }
        Vec3d cam = camera.getPos();
        drift += context.tickDelta() * 0.001f;

        MatrixStack.Entry entry = context.matrixStack().peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();
        Matrix4f projectionMatrix = context.projectionMatrix();

        for (float height : CloudConfig.EXTRA_HEIGHTS) {
            renderLayer(positionMatrix, projectionMatrix, cam, height);
        }
    }

    /** 画一层云海平面：以相机为中心、半径 LAYER_RADIUS、Y=height 的半透明四边形 */
    private static void renderLayer(Matrix4f positionMatrix, Matrix4f projectionMatrix,
                                    Vec3d cam, float height) {
        float r = CloudConfig.LAYER_RADIUS;
        float uvScale = r / CloudConfig.UV_PER_BLOCK;
        float u = drift;

        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, CLOUDS_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1f, 1f, 1f, CloudConfig.LAYER_ALPHA);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        float x0 = (float) (cam.x - r);
        float z0 = (float) (cam.z - r);
        float x1 = (float) (cam.x + r);
        float z1 = (float) (cam.z + r);

        buffer.vertex(positionMatrix, x0, height, z0).texture(u, u).color(1f, 1f, 1f, 1f).next();
        buffer.vertex(positionMatrix, x1, height, z0).texture(u + uvScale, u).color(1f, 1f, 1f, 1f).next();
        buffer.vertex(positionMatrix, x1, height, z1).texture(u + uvScale, u + uvScale).color(1f, 1f, 1f, 1f).next();
        buffer.vertex(positionMatrix, x0, height, z1).texture(u, u + uvScale).color(1f, 1f, 1f, 1f).next();

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
    }
}
