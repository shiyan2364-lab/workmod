package com.shiyan2364.workmod.item;

import com.shiyan2364.workmod.item.curse.CurseItem;
import com.shiyan2364.workmod.item.curse.CurseTrinket;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 诅咒饰品注册（阶段：诅咒系统可获取化）
 * <p>
 * 注册主戒「探险者之悲」+ 7 件伴生诅咒饰品（各绑一个极致系列）。
 * 强度不写死在物品上 —— tooltip 实时读取 CurseItem.getStrength，
 * 自动跟随极致进度（无=-100% / Ⅰ=-50% / Ⅱ=+100% / Ⅲ=+900%）。
 */
public final class CurseItems {

    public static final Item MAIN_RING = register("curse.main_ring", CurseItem.CurseType.MAIN_RING);
    public static final Item GREED_HAND = register("curse.greed_hand", CurseItem.CurseType.GREED_HAND);
    public static final Item ENVY_EYE = register("curse.envy_eye", CurseItem.CurseType.ENVY_EYE);
    public static final Item WRATH_HAMMER = register("curse.wrath_hammer", CurseItem.CurseType.WRATH_HAMMER);
    public static final Item SLOTH_STONE = register("curse.sloth_stone", CurseItem.CurseType.SLOTH_STONE);
    public static final Item PRIDE_CROWN = register("curse.pride_crown", CurseItem.CurseType.PRIDE_CROWN);
    public static final Item LUST_FLOWER = register("curse.lust_flower", CurseItem.CurseType.LUST_FLOWER);
    public static final Item FEAR_HEART = register("curse.fear_heart", CurseItem.CurseType.FEAR_HEART);

    private CurseItems() {
    }

    /** 由 Workmod.onInitialize 调用，保证类加载（静态字段此时完成注册） */
    public static void registerAll() {
        // 静态字段初始化即注册；此处仅用于触发类加载
    }

    private static Item register(String path, CurseItem.CurseType type) {
        return Registry.register(Registries.ITEM, new Identifier("workmod", path),
                new CurseTrinket(new Item.Settings().maxCount(1), type));
    }
}
