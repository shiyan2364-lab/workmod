package com.shiyan2364.workmod.item.compat;

import com.google.common.collect.ImmutableMultimap;
import com.shiyan2364.workmod.item.CurseItems;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;

/**
 * 通用饰品栏兼容桥（Trinkets，软依赖）
 * <p>
 * 基于 Trinkets 3.7.2 真实 API（已从 jar 字节码核对）：
 * Trinket 接口：tick / onEquip / onUnequip / canEquip / canUnequip /
 *              getModifiers(→Multimap) / onBreak / getDropRule。
 * <p>
 * 代理对这些方法给出安全默认值（getModifiers 返回空 Multimap，
 * getDropRule 返回 DropRule.DEFAULT，canEquip 返回 true），
 * 8 件诅咒饰品注册为可入 Trinkets 饰品栏；强度调节仍走物品右键。
 * 未装 Trinkets 时静默跳过，不影响编译与运行。
 */
public final class TrinketCompat {

    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final String TRINKETS_ID = "trinkets";
    private static boolean attempted = false;

    private TrinketCompat() {
    }

    /** 由 Workmod.onInitialize 调用 */
    public static void registerAll() {
        if (!FabricLoader.getInstance().isModLoaded(TRINKETS_ID)) {
            return;
        }
        if (attempted) {
            return;
        }
        attempted = true;
        try {
            Class<?> trinketCls = Class.forName("dev.emi.trinkets.api.Trinket");
            Class<?> apiCls = Class.forName("dev.emi.trinkets.api.TrinketsApi");
            Class<?> dropRuleCls = Class.forName("dev.emi.trinkets.api.TrinketEnums$DropRule");
            Object defaultDropRule = java.lang.Enum.valueOf(
                    (Class<? extends java.lang.Enum>) dropRuleCls, "DEFAULT");
            Method register = apiCls.getMethod("registerTrinket", Item.class, trinketCls);

            Object trinket = Proxy.newProxyInstance(TrinketCompat.class.getClassLoader(),
                    new Class[]{trinketCls},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "canEquip":
                            case "canUnequip":
                                return true;
                            case "getModifiers":
                                return ImmutableMultimap.of();
                            case "getDropRule":
                                return defaultDropRule;
                            case "getTrinketItemRenderer":
                                return Optional.empty();
                            case "tick":
                            case "onEquip":
                            case "onUnequip":
                            case "onBreak":
                                return null;
                            default:
                                Class<?> ret = method.getReturnType();
                                if (ret == boolean.class) {
                                    return false;
                                }
                                if (ret == int.class) {
                                    return 0;
                                }
                                if (ret == float.class) {
                                    return 0f;
                                }
                                if (ret == double.class) {
                                    return 0d;
                                }
                                if (ret == long.class) {
                                    return 0L;
                                }
                                return null;
                        }
                    });

            Item[] items = {
                    CurseItems.MAIN_RING, CurseItems.GREED_HAND, CurseItems.ENVY_EYE,
                    CurseItems.WRATH_HAMMER, CurseItems.SLOTH_STONE, CurseItems.PRIDE_CROWN,
                    CurseItems.LUST_FLOWER, CurseItems.FEAR_HEART
            };
            for (Item item : items) {
                register.invoke(null, item, trinket);
            }
            LOGGER.info("[Workmod] Trinkets 饰品栏兼容已启用（8 件诅咒饰品可入栏）");
        } catch (Exception e) {
            LOGGER.warn("[Workmod] Trinkets 兼容注册失败（不影响游戏）: {}", e.getMessage());
        }
    }
}
