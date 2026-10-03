package com.shiyan2364.workmod.quest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务管理器
 * 主线8章：苏醒->古道->深渊->苍穹->觉醒->圣器->诅咒->重铸
 * 支线按NPC类型发布
 */
public class QuestManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");

    // 主线章节
    public static final String[] MAIN_QUESTS = {
            "苏醒：找到第一座宏伟建筑",
            "古道：修路连接2座建筑",
            "深渊：深入峡谷<-200",
            "苍穹：登顶高山>2700",
            "觉醒：集齐4个极致系列Ⅰ",
            "圣器：找到并开启4个圣器遗迹",
            "诅咒：完成主戒反转（4系Ⅱ）",
            "重铸：击败太古巨神，重铸世界"
    };

    private static int currentChapter = 0;
    private static boolean questComplete = false;

    /**
     * 获取当前主线任务
     */
    public static String getCurrentQuest() {
        if (currentChapter >= MAIN_QUESTS.length) {
            return "【主线完结】世界已重铸";
        }
        return MAIN_QUESTS[currentChapter];
    }

    /**
     * 推进主线
     */
    public static void advanceChapter() {
        if (currentChapter < MAIN_QUESTS.length - 1) {
            currentChapter++;
            LOGGER.info("[Workmod] 主线推进到第{}章: {}", currentChapter + 1, MAIN_QUESTS[currentChapter]);
        } else {
            questComplete = true;
            LOGGER.info("[Workmod] 主线全部完成！世界重铸！");
        }
    }

    public static int getCurrentChapter() { return currentChapter; }
    public static boolean isQuestComplete() { return questComplete; }

    public static void reset() {
        currentChapter = 0;
        questComplete = false;
    }
}
