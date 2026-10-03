package com.shiyan2364.workmod.world.road;

import com.shiyan2364.workmod.api.StructureStyle;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.HashMap;
import java.util.Map;

/**
 * 道路方块映射表
 * 根据建筑风格决定道路主方块 / 边缘装饰 / 桥面
 * 全部使用原版方块
 */
public class RoadBlockTable {
    private static final Map<StructureStyle, RoadStyle> TABLE = new HashMap<>();

    static {
        TABLE.put(StructureStyle.VILLAGE, new RoadStyle(
                Blocks.DIRT_PATH, Blocks.TORCH, Blocks.OAK_SLAB
        ));
        TABLE.put(StructureStyle.FORTRESS, new RoadStyle(
                Blocks.STONE_BRICKS, Blocks.REDSTONE_TORCH, Blocks.IRON_BARS
        ));
        TABLE.put(StructureStyle.RUIN, new RoadStyle(
                Blocks.CRACKED_STONE_BRICKS, Blocks.AIR, Blocks.CAMPFIRE
        ));
        TABLE.put(StructureStyle.OCEAN, new RoadStyle(
                Blocks.PRISMARINE, Blocks.SEA_LANTERN, Blocks.PRISMARINE_SLAB
        ));
        TABLE.put(StructureStyle.SNOW, new RoadStyle(
                Blocks.SNOW_BLOCK, Blocks.TORCH, Blocks.SPRUCE_SLAB
        ));
        TABLE.put(StructureStyle.MOUNTAIN, new RoadStyle(
                Blocks.COBBLESTONE, Blocks.TORCH, Blocks.STONE_SLAB
        ));
    }

    public static RoadStyle getStyle(StructureStyle style) {
        return TABLE.getOrDefault(style, new RoadStyle(
                Blocks.DIRT_PATH, Blocks.TORCH, Blocks.OAK_SLAB
        ));
    }

    public record RoadStyle(Block mainBlock, Block edgeBlock, Block bridgeBlock) {
    }
}
