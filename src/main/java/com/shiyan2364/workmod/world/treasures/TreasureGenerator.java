package com.shiyan2364.workmod.world.treasures;

import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 补给/宝藏生成器
 * 规则：
 * - 补给点间距 200 格
 * - 宝藏间隔 ≥100 格（不聚集）
 * - 稀有度：普通50% / 稀有30% / 传说15% / 神话5%
 */
public class TreasureGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final Random RANDOM = new Random();
    private static final int SUPPLY_SPACING = 200;
    private static final int TREASURE_MIN_SPACING = 100;

    private final List<BlockPos> supplyPositions = new ArrayList<>();
    private final List<BlockPos> treasurePositions = new ArrayList<>();

    /**
     * 生成补给点（沿道路/建筑附近）
     */
    public SupplyPoint generateSupply(BlockPos center) {
        // 检查间距
        for (BlockPos pos : supplyPositions) {
            if (center.isWithinDistance(pos, SUPPLY_SPACING)) {
                return null; // 太近，不生成
            }
        }

        SupplyPoint.SupplyType type = SupplyPoint.SupplyType.values()[RANDOM.nextInt(SupplyPoint.SupplyType.values().length)];
        SupplyPoint point = new SupplyPoint(center, type);
        supplyPositions.add(center);
        LOGGER.info("[Workmod] 补给点生成 @ {} 类型: {}", center, type.getDisplayName());
        return point;
    }

    /**
     * 生成宝藏点
     */
    public TreasurePoint generateTreasure(BlockPos center) {
        // 检查最小间距
        for (BlockPos pos : treasurePositions) {
            if (center.isWithinDistance(pos, TREASURE_MIN_SPACING)) {
                return null; // 太近，不生成
            }
        }

        TreasurePoint.TreasureRarity rarity = randomRarity();
        TreasurePoint point = new TreasurePoint(center, rarity);
        treasurePositions.add(center);
        LOGGER.info("[Workmod] 宝藏点生成 @ {} 稀有度: {}", center, rarity.getDisplayName());
        return point;
    }

    /**
     * 按权重随机稀有度
     */
    private TreasurePoint.TreasureRarity randomRarity() {
        int roll = RANDOM.nextInt(100);
        if (roll < 50) return TreasurePoint.TreasureRarity.COMMON;
        if (roll < 80) return TreasurePoint.TreasureRarity.RARE;
        if (roll < 95) return TreasurePoint.TreasureRarity.LEGENDARY;
        return TreasurePoint.TreasureRarity.MYTHIC;
    }
}
