package com.shiyan2364.workmod.world.treasures;

import com.shiyan2364.workmod.world.road.RoadNetwork;
import com.shiyan2364.workmod.world.road.RoadSegment;
import com.shiyan2364.workmod.world.road.RoadType;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 补给/宝藏自动生成器
 * 沿主路每隔200格放补给箱（木箱）
 * 建筑附近放宝藏箱（稀有度按概率）
 * 已放置位置记录在内存Set，避免重复
 */
public class TreasureAutoSpawner {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final Set<String> PROCESSED_POS = new HashSet<>();
    private static final Random RANDOM = new Random();
    private static final int SUPPLY_SPACING = 200;
    private static final int TREASURE_RADIUS = 20;

    public static void tick(ServerWorld world) {
        if (world.isClient) return;

        // 只在主世界生成
        if (!world.getRegistryKey().getValue().equals(net.minecraft.util.Identifier.of("minecraft", "overworld"))) return;

        // 扫描路网
        for (RoadSegment seg : RoadNetwork.getInstance().getAllSegments()) {
            List<BlockPos> pts = seg.getPoints();
            if (pts.isEmpty()) continue;

            // 沿主路每隔SUPPLY_SPACING放补给
            if (seg.getType() == RoadType.MAIN) {
                for (int i = 0; i < pts.size(); i += SUPPLY_SPACING) {
                    if (i >= pts.size()) break;
                    BlockPos pos = pts.get(i);
                    if (!PROCESSED_POS.contains(pos.toShortString())) {
                        placeSupply(world, pos);
                        PROCESSED_POS.add(pos.toShortString());
                    }
                }
            }
        }

        // 建筑附近宝藏已交给结构生成后处理，这里留空（后续接入）
    }

    /**
     * 放置补给箱（木箱+火焰粒子效果占位）
     */
    private static void placeSupply(ServerWorld world, BlockPos pos) {
        if (!world.isAir(pos)) return;

        world.setBlockState(pos, Blocks.CHEST.getDefaultState());
        // 填内容
        if (world.getBlockEntity(pos) instanceof Inventory inv) {
            fillSupplyChest(inv);
        }

        // 简单粒子提示（绿色粒子）
        world.spawnParticles(net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER,
                pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 3, 0.3, 0.3, 0.3, 0);

        LOGGER.info("[Workmod] 补给箱放置 @ {}", pos);
    }

    /**
     * 填充补给内容（原版物品）
     */
    private static void fillSupplyChest(Inventory inv) {
        SupplyPoint.SupplyType type = SupplyPoint.SupplyType.values()[RANDOM.nextInt(SupplyPoint.SupplyType.values().length)];

        switch (type) {
            case FOOD -> {
                inv.setStack(0, new ItemStack(Items.BREAD, 8));
                inv.setStack(1, new ItemStack(Items.APPLE, 3 + RANDOM.nextInt(3)));
            }
            case TOOLS -> {
                inv.setStack(0, new ItemStack(Items.IRON_PICKAXE));
                inv.setStack(1, new ItemStack(Items.IRON_AXE));
                inv.setStack(2, new ItemStack(Items.TORCH, 32));
            }
            case BLOCKS -> {
                inv.setStack(0, new ItemStack(Items.STONE, 64));
                inv.setStack(1, new ItemStack(Items.OAK_PLANKS, 32));
            }
            case WATER -> {
                inv.setStack(0, new ItemStack(Items.WATER_BUCKET));
                inv.setStack(1, new ItemStack(Items.BUCKET));
            }
            case MIXED -> {
                inv.setStack(0, new ItemStack(Items.BREAD, 4));
                inv.setStack(1, new ItemStack(Items.IRON_PICKAXE));
                inv.setStack(2, new ItemStack(Items.TORCH, 16));
            }
        }
    }

    /**
     * 放置宝藏箱（带稀有度分级内容）
     */
    public static void placeTreasure(ServerWorld world, BlockPos pos, TreasurePoint.TreasureRarity rarity) {
        if (!PROCESSED_POS.contains(pos.toShortString())) {
            world.setBlockState(pos, Blocks.CHEST.getDefaultState());
            if (world.getBlockEntity(pos) instanceof Inventory inv) {
                fillTreasureChest(inv, rarity);
            }
            PROCESSED_POS.add(pos.toShortString());

            // 粒子：金色+紫色（宝藏感）
            world.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD,
                    pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 5, 0.3, 0.3, 0.3, 0);

            LOGGER.info("[Workmod] 宝藏箱放置 @ {} ({})", pos, rarity.getDisplayName());
        }
    }

    private static void fillTreasureChest(Inventory inv, TreasurePoint.TreasureRarity rarity) {
        switch (rarity) {
            case COMMON -> {
                inv.setStack(0, new ItemStack(Items.GOLD_INGOT, 3));
                inv.setStack(1, new ItemStack(Items.BREAD, 8));
            }
            case RARE -> {
                inv.setStack(0, new ItemStack(Items.DIAMOND, 5));
                inv.setStack(1, new ItemStack(Items.IRON_CHESTPLATE));
            }
            case LEGENDARY -> {
                inv.setStack(0, new ItemStack(Items.NETHERITE_INGOT, 2));
                inv.setStack(1, new ItemStack(Items.ENCHANTED_BOOK));
            }
            case MYTHIC -> {
                inv.setStack(0, new ItemStack(Items.DRAGON_EGG));
                inv.setStack(1, new ItemStack(Items.ELYTRA));
            }
        }
    }

    public static void resetTracking() {
        PROCESSED_POS.clear();
    }
}
