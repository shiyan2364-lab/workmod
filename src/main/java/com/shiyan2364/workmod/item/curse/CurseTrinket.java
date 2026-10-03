package com.shiyan2364.workmod.item.curse;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 诅咒饰品（可获取化 + 调节集成在饰品上）
 * <p>
 * CurseItem 原本只是静态逻辑类（强度计算），从未做成可拿到的物品。
 * 本类把每件诅咒饰品做成真实 Item：
 * - 右键 = 循环调节强度档位（受反转区间约束：未反转只能负档，反转后可正档），
 *   同时显示绑定系列与当前强度；
 * - 潜行 + 右键 = 恢复自动（重新跟随极致进度）。
 * <p>
 * 注意：本类在主源码集（src/main），因 splitEnvironmentSourceSets 不能引用
 * 客户端类，故不重写 appendTooltip（信息走右键消息）。
 */
public class CurseTrinket extends Item {

    /** 强度档位（循环顺序） */
    private static final int[] LEVELS = {-100, -50, 0, 100, 900};

    private final CurseItem.CurseType type;

    public CurseTrinket(Settings settings, CurseItem.CurseType type) {
        super(settings);
        this.type = type;
    }

    public CurseItem.CurseType getCurseType() {
        return type;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            String series = type.getBoundSeries() == null
                    ? "无" : type.getBoundSeries().getDisplayName();
            if (user.isSneaking()) {
                // 潜行右键：恢复自动（跟随极致进度）
                CurseItem.resetToDefault(type);
                user.sendMessage(Text.literal("§a「" + type.getDisplayName()
                        + "」已恢复自动跟随极致进度"), false);
            } else {
                // 右键：循环强度档位（反转前只能负档，反转后可正档）
                boolean awakened = CurseManager.isFullAwakened();
                int current = CurseItem.getStrength(type);
                int next = nextLevel(current, awakened);
                CurseItem.setCustomStrength(type, next);
                user.sendMessage(Text.literal("§6「" + type.getDisplayName() + "」绑定 §f"
                        + series + " §7强度 " + (next >= 0 ? "§e+" : "§c") + next + "%（手动）"
                        + (awakened ? "" : " §7未反转，仅负档")), false);
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    /** 取下一个允许的档位（未反转跳过正档） */
    private static int nextLevel(int current, boolean awakened) {
        for (int lv : LEVELS) {
            if (!awakened && lv > 0) {
                continue;
            }
            if (lv > current) {
                return lv;
            }
        }
        // 到头了回起点
        for (int lv : LEVELS) {
            if (!awakened && lv > 0) {
                continue;
            }
            return lv;
        }
        return 0;
    }
}
