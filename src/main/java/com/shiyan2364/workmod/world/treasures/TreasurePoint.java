package com.shiyan2364.workmod.world.treasures;

import net.minecraft.util.math.BlockPos;

/**
 * 宝藏点数据
 * 稀有奖励，间隔≥100格分散生成
 * 稀有度：普通50% / 稀有30% / 传说15% / 神话5%
 */
public class TreasurePoint {
    private final BlockPos pos;
    private final TreasureRarity rarity;
    private boolean claimed;

    public enum TreasureRarity {
        COMMON("普通", 50),
        RARE("稀有", 30),
        LEGENDARY("传说", 15),
        MYTHIC("神话", 5);

        private final String displayName;
        private final int weight;

        TreasureRarity(String displayName, int weight) {
            this.displayName = displayName;
            this.weight = weight;
        }

        public String getDisplayName() { return displayName; }
        public int getWeight() { return weight; }
    }

    public TreasurePoint(BlockPos pos, TreasureRarity rarity) {
        this.pos = pos;
        this.rarity = rarity;
        this.claimed = false;
    }

    public BlockPos getPos() { return pos; }
    public TreasureRarity getRarity() { return rarity; }
    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }
}
