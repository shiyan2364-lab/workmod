package com.shiyan2364.workmod.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ExplorerCompass extends Item {
    public ExplorerCompass(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return TypedActionResult.pass(user.getStackInHand(hand));
        ItemStack stack = user.getStackInHand(hand);
        int target = stack.getOrCreateNbt().getInt("TargetType");
        int next = (target + 1) % 3; // 简单循环
        stack.getOrCreateNbt().putInt("TargetType", next);
        user.sendMessage(Text.literal("切换目标: " + next), false);
        return TypedActionResult.success(stack);
    }
}
