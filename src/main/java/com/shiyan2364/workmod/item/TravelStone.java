package com.shiyan2364.workmod.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TravelStone extends Item {
    private final TravelStoneType stoneType;

    public TravelStone(Settings settings, TravelStoneType stoneType) {
        super(settings);
        this.stoneType = stoneType;
    }

    public TravelStoneType getStoneType() {
        return stoneType;
    }
}
