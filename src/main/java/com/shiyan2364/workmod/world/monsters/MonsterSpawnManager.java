package com.shiyan2364.workmod.world.monsters;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MonsterSpawnManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final Map<String, SpawnData> ACTIVE_SPAWNS = new HashMap<>();

    private record SpawnData(ServerWorld world, BlockPos pos, int count, int leaderCount, EntityType<?> type) {}

    public static void spawnGuards(ServerWorld world, BlockPos buildingPos, int magnificence, GuardMonsterType type) {
        if (world == null || world.isClient()) return;

        int count = switch (magnificence) {
            case 1, 2 -> 1;
            case 3, 4 -> 2;
            case 5, 6 -> 4;
            case 7, 8 -> 6;
            case 9 -> 8;
            default -> 10;
        };
        int leaderCount = magnificence >= 4 ? 1 : 0;

        for (int i = 0; i < count; i++) {
            MobEntity mob = (MobEntity) getEntityTypeForStyle(type).create(world);
            if (mob == null) continue;
            BlockPos spawnPos = buildingPos.add(world.random.nextInt(7) - 3, 1, world.random.nextInt(7) - 3);
            mob.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            mob.setPersistent();

            MonsterAttributes attrs = MonsterAttributes.fromMagnificence(magnificence);
            setHealthAttr(mob, attrs.getHealth());
            GuardVisualHandler.attachToMob(mob, magnificence, type.getDisplayName());
            world.spawnEntity(mob);
        }

        for (int i = 0; i < leaderCount; i++) {
            MobEntity leader = (MobEntity) getEntityTypeForStyle(type).create(world);
            if (leader == null) continue;
            BlockPos spawnPos = buildingPos.add(0, 2, 0);
            leader.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            leader.setPersistent();

            MonsterAttributes attrs = MonsterAttributes.fromMagnificence(magnificence + 2);
            setHealthAttr(leader, attrs.getHealth() * 2);
            GuardVisualHandler.attachToMob(leader, magnificence, "★首领·" + type.getDisplayName());
            world.spawnEntity(leader);
        }

        String key = buildingPos.toShortString();
        ACTIVE_SPAWNS.put(key, new SpawnData(world, buildingPos, count, leaderCount, getEntityTypeForStyle(type)));
        LOGGER.info("[Workmod] 宏伟度{} @{} 守卫{} 首领{} 已生成", magnificence, buildingPos, count, leaderCount);
    }

    private static void setHealthAttr(MobEntity mob, double health) {
        EntityAttributeInstance attr = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(health);
            mob.setHealth((float) health);
        }
    }

    private static EntityType<?> getEntityTypeForStyle(GuardMonsterType type) {
        return switch (type) {
            case CORRUPTED_VILLAGER -> EntityType.ZOMBIE;
            case IRON_RAIDER -> EntityType.VINDICATOR;
            case WRAITH_GUARD -> EntityType.WITHER_SKELETON;
            case DEEP_DIVER -> EntityType.DROWNED;
            case FROST_TROLL -> EntityType.STRAY;
            case STONE_GOLEM -> EntityType.IRON_GOLEM;
            case LAVA_GOLEM -> EntityType.MAGMA_CUBE;
            case VOID_WATCHER -> EntityType.SHULKER;
            default -> EntityType.ZOMBIE;
        };
    }

    public static void clearAllSpawns() {
        ACTIVE_SPAWNS.clear();
    }
}
