package com.shiyan2364.workmod.item.curse;

/**
 * 诅咒饰品 Lore 文案
 * 未反转（红色） / 觉醒（金色）
 */
public class CurseLore {

    /**
     * 获取主戒Lore
     */
    public static String[] getMainRingLore(int strength) {
        if (strength < 0) {
            return new String[]{
                    "§c\"所有踏上此路者，终将背负先辈的遗憾。\"",
                    "§c诅咒尚未解除：生命力、攻击、速度尽数流失。",
                    "§7完成极致成就，方能抚平戒指的裂隙。",
                    "§7当前强度: §c" + strength + "%"
            };
        } else {
            return new String[]{
                    "§6\"他们走过的路，由你走完；他们未竟的梦，由你实现。\"",
                    "§6探险者之悲已经觉醒——先辈的祝福在你血脉中奔涌。",
                    "§7你，是行走于世界脊梁上的传奇。",
                    "§7当前强度: §e+" + strength + "%"
            };
        }
    }

    /**
     * 获取伴生饰品Lore
     */
    public static String[] getCurseItemLore(CurseItem.CurseType type, int strength) {
        String curseLore = switch (type) {
            case GREED_HAND -> "谷底的财富，属于深渊。";
            case ENVY_EYE -> "窥探星辰者，必被星辰蒙蔽。";
            case WRATH_HAMMER -> "怒火灼烧己身，才能伤及万物。";
            case SLOTH_STONE -> "岁月流转，唯我停滞不前。";
            case PRIDE_CROWN -> "凌驾众生者，不屑凡人食粮。";
            case LUST_FLOWER -> "花之艳色，引万物觊觎。";
            case FEAR_HEART -> "深海之下，是永恒的牢笼。";
            default -> "";
        };

        String awakenLore = switch (type) {
            case GREED_HAND -> "从深渊归来者，连虚空都为之倾倒。";
            case ENVY_EYE -> "我凝视星海，星海也凝视我。";
            case WRATH_HAMMER -> "熔岩在我血脉中奔流。";
            case SLOTH_STONE -> "沧海桑田，我信步而行。";
            case PRIDE_CROWN -> "万物滋养我，我予万物以回应。";
            case LUST_FLOWER -> "我是春日，我令百花绽放。";
            case FEAR_HEART -> "万浪臣服于我足下。";
            default -> "";
        };

        if (strength < 0) {
            return new String[]{
                    "§c\"" + curseLore + "\"",
                    "§7当前强度: §c" + strength + "%",
                    "§7绑定: §f" + type.getBoundSeries().getDisplayName() + "系列"
            };
        } else {
            return new String[]{
                    "§6\"" + awakenLore + "\"",
                    "§7当前强度: §e+" + strength + "%",
                    "§7绑定: §f" + type.getBoundSeries().getDisplayName() + "系列"
            };
        }
    }
}
