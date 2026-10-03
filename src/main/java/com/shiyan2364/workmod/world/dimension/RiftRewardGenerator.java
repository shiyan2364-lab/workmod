package com.shiyan2364.workmod.world.dimension;

import com.shiyan2364.workmod.item.CurseItems;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 裂隙奖励生成器（诅咒饰品入裂隙）
 * <p>
 * 对每个已登记的裂隙门（RiftManager），在门旁放一口奖励箱：
 * 必含 2 件普通补给 + 概率含 1 件诅咒饰品（裂隙是高危探索的回报）。
 * 每个门只放一次。
 */
public final class RiftRewardGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");

    /** 每多少 tick 扫描一次裂隙（100 tick = 5 秒） */
    private static final int TICK_INTERVAL = 100;
    /** 诅咒饰品出现概率 */
    private static final double CURSE_CHANCE = 0.5;

    private static final Set<String> PROCESSED = new HashSet<>();
    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;

    private static final Item[] CURSE_DROPS = {
            CurseItems.MAIN_RING, CurseItems.GREED_HAND, CurseItems.ENVY_EYE,
            CurseItems.WRATH_HAMMER, CurseItems.SLOTH_STONE, CurseItems.PRIDE_CROWN,
            CurseItems.LUST_FLOWER, CurseItems.FEAR_HEART
    };

    private RiftRewardGenerator() {
    }

    /** 服务端每 tick 调用一次（由 Workmod 注册） */
    public static void tick(MinecraftServer server) {
        if (++tickCounter % TICK_INTERVAL != 0) {
            return;
        }
        ServerWorld world = server.getOverworld();
        if (world == null) {
            return;
        }
        for (RiftPortal portal : RiftManager.getAllPortals()) {
            BlockPos pos = portal.getOverworldPos();
            String key = (pos.getX() >> 4) + "," + (pos.getZ() >> 4);
            if (PROCESSED.contains(key)) {
                continue;
            }
            PROCESSED.add(key);
            placeReward(world, pos);
        }
    }

    /** 世界重载时重置 */
    public static void reset() {
        PROCESSED.clear();
    }

    private static void placeReward(ServerWorld world, BlockPos portalPos) {
        int px = portalPos.getX() + 2;
        int pz = portalPos.getZ() + 2;
        int py = world.getTopY(Heightmap.Type.MOTION_BLOCKING, px, pz);
        BlockPos chestPos = new BlockPos(px, py, pz);
        if (!world.isChunkLoaded(chestPos.getX() >> 4, chestPos.getZ() >> 4)) {
            return;
        }
        try {
            world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 3);
            if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                chest.setStack(0, new ItemStack(net.minecraft.item.Items.BREAD, 4));
                chest.setStack(1, new ItemStack(net.minecraft.item.Items.GOLDEN_APPLE));
                if (RANDOM.nextDouble() < CURSE_CHANCE) {
                    Item curse = CURSE_DROPS[RANDOM.nextInt(CURSE_DROPS.length)];
                    chest.setStack(2, new ItemStack(curse));
                }
            }
            LOGGER.info("[Workmod] 裂隙奖励箱 @ {}", chestPos);
        } catch (Exception e) {
            LOGGER.warn("[Workmod] 裂隙奖励箱失败: {}", e.getMessage());
        }
    }
}
