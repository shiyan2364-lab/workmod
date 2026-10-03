package com.shiyan2364.workmod.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

/**
 * 冒险者手册（阶段1b）
 * <p>
 * 用原版成书（WRITTEN_BOOK）改 NBT 制作，无新贴图。
 * 新玩家首次进入世界时由 Workmod 发放（ServerPlayConnectionEvents.JOIN）。
 */
public final class Handbook {

    public static final String TITLE = "冒险者手册";

    private Handbook() {
    }

    /** 生成一本手册（成书 NBT：title / author / pages） */
    public static ItemStack create() {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putString("title", TITLE);
        nbt.putString("author", "远古旅人");

        NbtList pages = new NbtList();
        pages.add(NbtString.of("{\"text\":\"§6§l冒险路网\\n\\n远古文明用古道连接宏伟建筑，七件诅咒圣物维系世界平衡。\\n你，将成为重铸世界的旅者。\"}"));
        pages.add(NbtString.of("{\"text\":\"§e第一章 启程\\n\\n§7沿路而行：\\n- 村庄、堡垒、古城间的古道会随你探索自动出现\\n- 补给箱与宝藏藏在路边\\n- 守卫把守要道，越强越危险\\n\\n§f输入 §b/workmod:help §f查看全部命令\"}"));
        pages.add(NbtString.of("{\"text\":\"§e第二章 路与断路\\n\\n§7古道并非总是完整：\\n悬崖、篝火桥、藤蔓与遗迹会把路断开。\\n越过断路继续向前，奖励在路的尽头。\"}"));
        pages.add(NbtString.of("{\"text\":\"§e第三章 裂隙\\n\\n§7在主世界的道路尽头，黑曜石裂隙通向地狱与末地。\\n每座门都有名字——熔岩之门、星辉之门。\\n\\n§f传送石碑可将你送回已探索的裂隙。\"}"));
        pages.add(NbtString.of("{\"text\":\"§e第四章 诅咒圣物\\n\\n§7七件诅咒饰品：贪婪之手、嫉妒之眼、暴怒之锤、怠惰之石、傲慢之冠、色欲之花、恐惧之心。\\n完成极致成就可唤醒沉睡的建筑，反转诅咒。\\n\\n§f比例：Ⅰ:Ⅱ:Ⅲ = 1:8:64\"}"));
        pages.add(NbtString.of("{\"text\":\"§b愿古道指引你。\\n—— 远古旅人\"}"));
        nbt.put("pages", pages);

        return stack;
    }

    /** 判断物品是否为本模组手册 */
    public static boolean isHandbook(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!stack.isOf(Items.WRITTEN_BOOK)) {
            return false;
        }
        NbtCompound nbt = stack.getNbt();
        return nbt != null && TITLE.equals(nbt.getString("title"));
    }
}
