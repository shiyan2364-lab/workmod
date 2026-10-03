package com.shiyan2364.workmod.item.curse;

import com.shiyan2364.workmod.item.CurseItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/**
 * 主戒「探险者之悲」等级与饰品栏槽数（自研轻量饰品栏）
 * <p>
 * 主戒自带等级 NBT（workmod_ring_level，0~6）：
 * 饰品栏槽数 = 基础 2 + 等级（上限 8 格）。
 * 升级用 /workmod:ring upgrade（消耗 1 级经验），等级写在戒指上、随戒指走。
 */
public final class CurseRingHelper {

    public static final String TAG_LEVEL = "workmod_ring_level";
    public static final int MAX_LEVEL = 6;
    public static final int BASE_SLOTS = 2;
    public static final int MAX_SLOTS = 8;

    private CurseRingHelper() {
    }

    public static int getLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        NbtCompound nbt = stack.getNbt();
        return nbt == null ? 0 : nbt.getInt(TAG_LEVEL);
    }

    public static void setLevel(ItemStack stack, int level) {
        stack.getOrCreateNbt().putInt(TAG_LEVEL, Math.max(0, Math.min(MAX_LEVEL, level)));
    }

    /** 等级 → 饰品栏槽数 */
    public static int slotsForLevel(int level) {
        return Math.min(MAX_SLOTS, BASE_SLOTS + Math.max(0, level));
    }

    public static boolean isMainRing(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.isOf(CurseItems.MAIN_RING);
    }
}
