package com.shiyan2364.workmod.event;

import com.shiyan2364.workmod.item.CurseItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

import java.util.Random;

/**
 * 诅咒饰品掉落监听（阶段：诅咒系统可获取化）
 * <p>
 * 击杀敌对生物时有概率掉落诅咒饰品（8 件随机）：
 * - 首领守卫（名字以 ★ 开头，MonsterSpawnManager 的 "★首领·xxx"）：50% 掉落；
 * - 普通敌对生物：3% 掉落（低概率，防刷怪塔刷饰品）。
 * <p>
 * 对应说明书玩法：击杀守卫 → 掉落 → 成就解锁更强诅咒。
 */
public final class CurseDropListener {

    private static final Random RANDOM = new Random();
    private static final double BOSS_CHANCE = 0.5;
    private static final double NORMAL_CHANCE = 0.03;

    private static final Item[] DROPS = {
            CurseItems.MAIN_RING, CurseItems.GREED_HAND, CurseItems.ENVY_EYE,
            CurseItems.WRATH_HAMMER, CurseItems.SLOTH_STONE, CurseItems.PRIDE_CROWN,
            CurseItems.LUST_FLOWER, CurseItems.FEAR_HEART
    };

    private CurseDropListener() {
    }

    /** 由 Workmod.onInitialize 调用 */
    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            try {
                if (entity == null || entity.getWorld().isClient) {
                    return;
                }
                if (!(entity instanceof HostileEntity)) {
                    return;
                }
                boolean isBoss = entity.getCustomName() != null
                        && entity.getCustomName().getString().startsWith("★");
                double chance = isBoss ? BOSS_CHANCE : NORMAL_CHANCE;
                if (RANDOM.nextDouble() > chance) {
                    return;
                }
                ServerWorld world = (ServerWorld) entity.getWorld();
                Item item = DROPS[RANDOM.nextInt(DROPS.length)];
                ItemStack stack = new ItemStack(item);
                ItemEntity drop = new ItemEntity(world,
                        entity.getX(), entity.getY() + 0.5, entity.getZ(), stack);
                drop.setToDefaultPickupDelay();
                world.spawnEntity(drop);
            } catch (Exception ignored) {
                // 掉落失败不影响游戏
            }
        });
    }
}
