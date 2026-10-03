package com.shiyan2364.workmod.npc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * NPC管理器
 * 管理每位NPC的觉醒等级/声望/可交易内容
 */
public class NpcManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final Map<NpcType, NpcData> NPCS = new HashMap<>();

    private static class NpcData {
        AwakeningLevel awakening;
        int reputation;
        boolean questActive;

        NpcData() {
            this.awakening = AwakeningLevel.SLEEPING;
            this.reputation = 0;
            this.questActive = false;
        }
    }

    static {
        for (NpcType type : NpcType.values()) {
            NPCS.put(type, new NpcData());
        }
    }

    /**
     * 提升NPC觉醒等级（击杀守卫/完成任务触发）
     */
    public static void raiseAwakening(NpcType type) {
        NpcData data = NPCS.get(type);
        if (data.awakening.getLevel() < 4) {
            data.awakening = AwakeningLevel.fromLevel(data.awakening.getLevel() + 1);
            LOGGER.info("[Workmod] {} 觉醒提升至 {}", type.getDisplayName(), data.awakening.getDisplayName());
        }
    }

    /**
     * 增加声望
     */
    public static void addReputation(NpcType type, int amount) {
        NpcData data = NPCS.get(type);
        data.reputation += amount;
        LOGGER.info("[Workmod] {} 声望 +{} (当前 {})", type.getDisplayName(), amount, data.reputation);
    }

    /**
     * 声望等级（决定折扣）
     * 陌生0 / 熟悉3任务 / 尊敬8 / 崇敬15 / 传说30
     */
    public static int getReputationTier(NpcType type) {
        int rep = NPCS.get(type).reputation;
        if (rep >= 30) return 4; // 传说 半价
        if (rep >= 15) return 3; // 崇敬 8折
        if (rep >= 8) return 2;  // 尊敬 特殊道具
        if (rep >= 3) return 1;  // 熟悉 9折
        return 0;                // 陌生
    }

    /**
     * 获取NPC当前等级
     */
    public static AwakeningLevel getAwakening(NpcType type) {
        return NPCS.get(type).awakening;
    }

    /**
     * 检查是否有权限购买等级道具
     */
    public static boolean canAccessTrade(NpcType type, int requiredLevel) {
        return NPCS.get(type).awakening.canAccess(requiredLevel);
    }

    public static void resetAll() {
        for (NpcType type : NpcType.values()) {
            NpcData data = NPCS.get(type);
            data.awakening = AwakeningLevel.SLEEPING;
            data.reputation = 0;
            data.questActive = false;
        }
    }
}
