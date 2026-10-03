package com.shiyan2364.workmod.client;

/**
 * 高空云层配置（阶段：云层高度优化）
 * <p>
 * 设计原则（对齐内容说明书）：
 * - 原版云层（Y≈128）保持不动 —— 模组不改原版观感；
 * - 模组额外渲染"高空云海"层，放在宏伟建筑/登山道的高度，
 *   让超高的模组内容有云相伴，而不是孤零零戳在天空里；
 * - 只渲染模组内容相关的高度，原版地形/云一概不动。
 * <p>
 * 调整方法：改下面的数组后重新编译（Actions）即可。
 * 想关闭高空云：把数组清空为 new float[0]。
 */
public final class CloudConfig {

    /** 高空云层高度列表（Y 轴，默认 300 / 480 / 700，可自行增删） */
    public static final float[] EXTRA_HEIGHTS = {300f, 480f, 700f};

    /** 每层云海平面的半径（格，覆盖视距即可） */
    public static final float LAYER_RADIUS = 600f;

    /** 高层云不透明度（0~1，远景用淡一点） */
    public static final float LAYER_ALPHA = 0.45f;

    /** 云纹理每多少世界格重复一次（32 格 = 原版一个云块周期） */
    public static final float UV_PER_BLOCK = 32f;

    private CloudConfig() {
    }
}
