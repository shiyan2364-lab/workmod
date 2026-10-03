package com.shiyan2364.workmod.item;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Waystones 兼容层
 * 检测是否装了 Waystones mod
 * - 装了：Rei 用其石碑传送（古老/魂系无CD）
 * - 没装：退化为纯路标（只能标记，不能传送）
 */
public class WaystoneCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final String WAYSTONES_ID = "waystones";

    private static Boolean waystonesLoaded = null;

    /**
     * 检测 Waystones 是否加载
     */
    public static boolean isWaystonesLoaded() {
        if (waystonesLoaded == null) {
            waystonesLoaded = FabricLoader.getInstance().isModLoaded(WAYSTONES_ID);
            if (waystonesLoaded) {
                LOGGER.info("[Workmod] 检测到 Waystones，传送石碑启用完整功能");
            } else {
                LOGGER.info("[Workmod] 未安装 Waystones，传送石碑退化为普通路标");
            }
        }
        return waystonesLoaded;
    }

    /**
     * 是否需要末影珍珠（有Waystones+非古老/魂系）才消耗
     */
    public static boolean shouldConsumePearl(TravelStoneType type) {
        if (!isWaystonesLoaded()) {
            return false; // 路标模式不消耗
        }
        return type.requiresPearl() && type.getCooldownTicks() > 0;
    }

    public static void resetCache() {
        waystonesLoaded = null;
    }
}
