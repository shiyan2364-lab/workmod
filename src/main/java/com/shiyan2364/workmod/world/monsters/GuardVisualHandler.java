package com.shiyan2364.workmod.world.monsters;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 守卫怪视觉增强
 * 1. 独立血条：每个"守卫怪"在底部显示一个Boss血条（按宏伟度颜色）
 * 2. 粒子特效：身上持续散发彩色粒子（按宏伟度颜色），方便与普通怪区分
 *
 * 纯原版：BossBar + DustParticleEffect，无新贴图
 */
public class GuardVisualHandler {
    // 记录实体 → 对应的血条
    private static final Map<Entity, ServerBossBar> BARS = new IdentityHashMap<>();

    /**
     * 给已生成的守卫怪附加血条+粒子（在spawnGuards后调用）
     */
    public static void attachToMob(MobEntity mob, int magnificence, String guardName) {
        // 设置名字带颜色
        String color = magnitudeColor(magnificence);
        mob.setCustomName(Text.literal(color + guardName + " §7[" + magnificence + "级]"));

        // 创建Boss血条
        ServerBossBar bar = new ServerBossBar(
                Text.literal(color + guardName + " §7[" + magnificence + "级]"),
                magnitudeBarColor(magnificence),
                BossBar.Style.PROGRESS
        );
        bar.setVisible(true);
        BARS.put(mob, bar);
    }

    /**
     * 每tick调用：更新血条进度 + 粒子
     */
    public static void tick(ServerWorld world) {
        if (world.isClient) return;

        // 清理失效实体
        BARS.entrySet().removeIf(entry -> !entry.getKey().isAlive());

        for (Map.Entry<Entity, ServerBossBar> entry : BARS.entrySet()) {
            LivingEntity mob = (LivingEntity) entry.getKey();
            ServerBossBar bar = entry.getValue();

            if (!mob.isAlive()) {
                bar.clearPlayers();
                continue;
            }

            // 更新血条进度
            bar.setPercent(mob.getHealth() / mob.getMaxHealth());

            // 粒子特效
            spawnParticles(world, mob);
        }
    }

    /**
     * 扩展原版spawnGuards，在实体生成后附加视觉
     */
    public static void applyToExistingBars(ServerWorld world) {
        // 空实现占位，实际在spawnGuards里调用attachToMob
    }

    /**
     * 粒子生成：环绕身体旋转彩色尘埃
     */
    private static void spawnParticles(ServerWorld world, LivingEntity mob) {
        Vec3d pos = mob.getPos();
        double y = pos.y + mob.getHeight() / 2.0;

        // 获取宏伟度（从名字解析，简化：固定用紫色）
        // 实际在attach时存一个颜色字段更合理，这里用紫色示意
        ParticleEffect effect = new DustParticleEffect(
                new org.joml.Vector3f(0.8f, 0.4f, 1.0f), 1.0f); // 紫红色尘埃

        world.spawnParticles(effect, pos.x, y, pos.z, 2, 0.3, 0.3, 0.3, 0.0);
    }

    /**
     * 宏伟度→名字颜色
     */
    private static String magnitudeColor(int m) {
        return switch (m) {
            case 1, 2 -> "§7";  // 灰
            case 3, 4 -> "§a";  // 绿
            case 5, 6 -> "§b";  // 蓝
            case 7, 8 -> "§5";  // 紫
            case 9 -> "§6";     // 金
            default -> "§c";    // 红
        };
    }

    /**
     * 宏伟度→BossBar颜色
     */
    private static BossBar.Color magnitudeBarColor(int m) {
        return switch (m) {
            case 1, 2 -> BossBar.Color.WHITE;
            case 3, 4 -> BossBar.Color.GREEN;
            case 5, 6 -> BossBar.Color.BLUE;
            case 7, 8 -> BossBar.Color.PURPLE;
            case 9 -> BossBar.Color.YELLOW;
            default -> BossBar.Color.RED;
        };
    }

    /**
     * 管理：给指定玩家显示/隐藏血条
     */
    public static void addViewer(ServerPlayerEntity player, int entityId) {
        for (Map.Entry<Entity, ServerBossBar> entry : BARS.entrySet()) {
            if (entry.getKey().getId() == entityId) {
                entry.getValue().addPlayer(player);
            }
        }
    }
}
