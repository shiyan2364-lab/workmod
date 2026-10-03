package com.shiyan2364.workmod.event;

import com.shiyan2364.workmod.world.monsters.RealTimeDifficulty;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.mob.Monster;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 死亡惩罚事件
 * 被怪杀死 → 扣大怪能量；环境死 → 扣小怪能量
 */
public class DeathListener {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity)) return;

            if (damageSource.getAttacker() instanceof Monster) {
                // 被怪杀死
                RealTimeDifficulty.getInstance().onDeath(true);
            } else {
                // 环境死（摔死/虚空/溺水/火烧）
                RealTimeDifficulty.getInstance().onEnvironmentalDeath();
            }
        });
    }
}
