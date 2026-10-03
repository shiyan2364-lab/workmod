package com.shiyan2364.workmod.data;

import com.shiyan2364.workmod.world.treasures.SupplyPoint;
import com.shiyan2364.workmod.world.treasures.TreasurePoint;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

public class TreasureSavedData extends PersistentState {
    private static final String DATA_NAME = "workmod_treasures";

    private static final List<BlockPos> cachedSupplies = new ArrayList<>();
    private static final List<BlockPos> cachedTreasures = new ArrayList<>();

    private final List<SupplyPoint> supplies = new ArrayList<>();
    private final List<TreasurePoint> treasures = new ArrayList<>();

    public static TreasureSavedData create() {
        return new TreasureSavedData();
    }


    public static TreasureSavedData fromNbt(NbtCompound nbt) {
        TreasureSavedData data = new TreasureSavedData();

        NbtList supplyList = nbt.getList("Supplies", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < supplyList.size(); i++) {
            NbtCompound comp = supplyList.getCompound(i);
            BlockPos pos = new BlockPos(comp.getInt("X"), comp.getInt("Y"), comp.getInt("Z"));
            SupplyPoint point = new SupplyPoint(pos, SupplyPoint.SupplyType.values()[comp.getInt("Type")]);
            point.setCollected(comp.getBoolean("Collected"));
            data.supplies.add(point);
            cachedSupplies.add(pos);
        }

        NbtList treasureList = nbt.getList("Treasures", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < treasureList.size(); i++) {
            NbtCompound comp = treasureList.getCompound(i);
            BlockPos pos = new BlockPos(comp.getInt("X"), comp.getInt("Y"), comp.getInt("Z"));
            TreasurePoint point = new TreasurePoint(pos, TreasurePoint.TreasureRarity.values()[comp.getInt("Rarity")]);
            point.setClaimed(comp.getBoolean("Claimed"));
            data.treasures.add(point);
            cachedTreasures.add(pos);
        }

        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList supplyList = new NbtList();
        for (SupplyPoint point : supplies) {
            NbtCompound comp = new NbtCompound();
            comp.putInt("X", point.getPos().getX());
            comp.putInt("Y", point.getPos().getY());
            comp.putInt("Z", point.getPos().getZ());
            comp.putInt("Type", point.getType().ordinal());
            comp.putBoolean("Collected", point.isCollected());
            supplyList.add(comp);
        }
        nbt.put("Supplies", supplyList);

        NbtList treasureList = new NbtList();
        for (TreasurePoint point : treasures) {
            NbtCompound comp = new NbtCompound();
            comp.putInt("X", point.getPos().getX());
            comp.putInt("Y", point.getPos().getY());
            comp.putInt("Z", point.getPos().getZ());
            comp.putInt("Rarity", point.getRarity().ordinal());
            comp.putBoolean("Claimed", point.isClaimed());
            treasureList.add(comp);
        }
        nbt.put("Treasures", treasureList);
        return nbt;
    }

    public void addSupply(SupplyPoint point) {
        supplies.add(point);
        cachedSupplies.add(point.getPos());
        markDirty();
    }

    public void addTreasure(TreasurePoint point) {
        treasures.add(point);
        cachedTreasures.add(point.getPos());
        markDirty();
    }

    public List<SupplyPoint> getAllSupplies() { return supplies; }
    public List<TreasurePoint> getAllTreasures() { return treasures; }

    // 罗盘用
    public static List<BlockPos> getCachedSupplies() { return cachedSupplies; }
    public static List<BlockPos> getCachedTreasures() { return cachedTreasures; }

    public static String getDataName() { return DATA_NAME; }
}
