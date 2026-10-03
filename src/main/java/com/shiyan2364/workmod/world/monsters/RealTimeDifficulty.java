package com.shiyan2364.workmod.world.monsters;

import com.shiyan2364.workmod.data.DifficultySavedData;

public class RealTimeDifficulty {
    private static final double SMALL_CAP = 200.0;
    private static final double SMALL_WEIGHT = 0.0001;
    private static final double LARGE_WEIGHT = 0.002;
    private static final long SMALL_DECAY_INTERVAL = 24000L * 3;
    private static final long LARGE_DECAY_INTERVAL = 24000L * 7;

    private static final RealTimeDifficulty INSTANCE = new RealTimeDifficulty();

    private double smallEnergy = 0;
    private double largeEnergy = 0;
    private long lastSmallDecay = 0;
    private long lastLargeDecay = 0;
    private boolean loaded = false;

    public static RealTimeDifficulty getInstance() { return INSTANCE; }

    public void loadFromData(DifficultySavedData data) {
        this.smallEnergy = data.getSmallEnergy();
        this.largeEnergy = data.getLargeEnergy();
        this.lastSmallDecay = data.getLastDecayTime();
        this.lastLargeDecay = data.getLastDecayTime();
        this.loaded = true;
    }

    public void saveToData(DifficultySavedData data) {
        data.setSmallEnergy(smallEnergy);
        data.setLargeEnergy(largeEnergy);
        data.setLastDecayTime(lastLargeDecay);
    }

    public void tick(long worldTime) {
        if (!loaded) return;
        if (worldTime - lastSmallDecay >= SMALL_DECAY_INTERVAL) {
            smallEnergy *= 0.9;
            lastSmallDecay = worldTime;
        }
        if (worldTime - lastLargeDecay >= LARGE_DECAY_INTERVAL) {
            largeEnergy *= 0.95;
            lastLargeDecay = worldTime;
        }
    }

    public void addSmallEnergy(double amount) { smallEnergy = Math.min(SMALL_CAP, smallEnergy + amount); }
    public void addLargeEnergy(double amount) { largeEnergy += amount; }

    public void onDeath(boolean killedByLarge) {
        if (killedByLarge) largeEnergy = Math.max(0, largeEnergy - 15);
        else smallEnergy = Math.max(0, smallEnergy - 0.6);
    }

    public void onEnvironmentalDeath() { smallEnergy = Math.max(0, smallEnergy - 1.0); }

    public double getTotalBonus() { return smallEnergy * SMALL_WEIGHT + largeEnergy * LARGE_WEIGHT; }

    public MonsterAttributes applyDifficulty(MonsterAttributes base) {
        double bonus = 1.0 + getTotalBonus();
        return new MonsterAttributes(
                Math.max(1, (int) (base.getHealth() * bonus)),
                Math.max(1, (int) (base.getDamage() * bonus)),
                base.getArmor(), base.getSpeed(), base.getMagnificence());
    }

    public double getSmallEnergy() { return smallEnergy; }
    public double getLargeEnergy() { return largeEnergy; }
    public boolean isLoaded() { return loaded; }
    public void reset() { smallEnergy = 0; largeEnergy = 0; }
}
