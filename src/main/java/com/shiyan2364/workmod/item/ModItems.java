package com.shiyan2364.workmod.item;

import com.shiyan2364.workmod.Workmod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 模组物品注册
 * 集中管理所有自定义物品（全部用原版物品改NBT/行为）
 */
public class ModItems {

    // 探路罗盘
    public static final Item EXPLORER_COMPASS = new ExplorerCompass(new Item.Settings().maxCount(1));

    // 传送石碑（4种）
    public static final Item UNNAMED_STONE = new TravelStone(new Item.Settings().maxCount(1), TravelStoneType.UNNAMED);
    public static final Item TRAVELER_STONE = new TravelStone(new Item.Settings().maxCount(1), TravelStoneType.TRAVELER);
    public static final Item ANCIENT_STONE = new TravelStone(new Item.Settings().maxCount(1), TravelStoneType.ANCIENT);
    public static final Item SOUL_STONE = new TravelStone(new Item.Settings().maxCount(1), TravelStoneType.SOUL);

    /**
     * 注册所有物品
     */
    public static void registerAll() {
        Registry.register(Registries.ITEM, Identifier.of(Workmod.MOD_ID, "explorer_compass"), EXPLORER_COMPASS);

        Registry.register(Registries.ITEM, Identifier.of(Workmod.MOD_ID, "unnamed_stone"), UNNAMED_STONE);
        Registry.register(Registries.ITEM, Identifier.of(Workmod.MOD_ID, "traveler_stone"), TRAVELER_STONE);
        Registry.register(Registries.ITEM, Identifier.of(Workmod.MOD_ID, "ancient_stone"), ANCIENT_STONE);
        Registry.register(Registries.ITEM, Identifier.of(Workmod.MOD_ID, "soul_stone"), SOUL_STONE);

        Workmod.LOGGER.info("[Workmod] 物品注册完成（含4种石碑）");
    }
}
