package com.shiyan2364.workmod.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public class DifficultySavedData extends PersistentState {
    private static final String DATA_NAME = "workmod_difficulty";
    private double smallEnergy = 0;
    private double largeEnergy = 0;
    private long lastDecayTime = 0;

    public static DifficultySavedData create() {
        return new DifficultySavedData();
    }


    public static DifficultySavedData fromNbt(NbtCompound nbt) {
        DifficultySavedData data = new DifficultySavedData();
        data.smallEnergy = nbt.getDouble("SmallEnergy");
        data.largeEnergy = nbt.getDouble("LargeEnergy");
        data.lastDecayTime = nbt.getLong("LastDecayTime");
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putDouble("SmallEnergy", smallEnergy);
        nbt.putDouble("LargeEnergy", largeEnergy);
        nbt.putLong("LastDecayTime", lastDecayTime);
        return nbt;
    }

    public double getSmallEnergy() { return smallEnergy; }
    public double getLargeEnergy() { return largeEnergy; }
    public long getLastDecayTime() { return lastDecayTime; }
    public void setSmallEnergy(double v) { this.smallEnergy = Math.min(200, v); markDirty(); }
    public void setLargeEnergy(double v) { this.largeEnergy = v; markDirty(); }
    public void setLastDecayTime(long t) { this.lastDecayTime = t; markDirty(); }
    public void reset() { smallEnergy = 0; largeEnergy = 0; markDirty(); }
    public static String getDataName() { return DATA_NAME; }
}
