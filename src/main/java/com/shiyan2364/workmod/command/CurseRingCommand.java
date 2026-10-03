package com.shiyan2364.workmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.shiyan2364.workmod.data.CurseInventorySavedData;
import com.shiyan2364.workmod.item.curse.CurseRingHelper;
import com.shiyan2364.workmod.item.curse.CurseTrinket;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * 随身饰品栏命令（自研轻量饰品栏，主戒扩栏）
 * <p>
 * 主戒「探险者之悲」自带等级 NBT（0~6）：饰品栏槽数 = 2 + 等级（上限 8）。
 * <p>
 * /workmod:ring list —— 查看槽位内容
 * /workmod:ring put <槽> —— 把手中诅咒饰品放入槽位
 * /workmod:ring take <槽> —— 取出槽位饰品
 * /workmod:ring upgrade —— 手持主戒，消耗 1 级经验提升主戒等级（扩栏）
 * <p>
 * 槽位数据持久化在 workmod_ring_inventory（离线不丢）。
 */
public final class CurseRingCommand {

    private CurseRingCommand() {
    }

    /** 由 Workmod.onInitialize 调用 */
    public static void register() {
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
                CurseRingCommand::onRegister);
    }

    private static void onRegister(CommandDispatcher<ServerCommandSource> dispatcher,
                                   CommandRegistryAccess registryAccess,
                                   CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("workmod")
                .then(literal("ring")
                        .then(literal("list").executes(ctx -> list(ctx.getSource())))
                        .then(literal("put")
                                .then(argument("slot", IntegerArgumentType.integer(0, 7))
                                        .executes(ctx -> put(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "slot")))))
                        .then(literal("take")
                                .then(argument("slot", IntegerArgumentType.integer(0, 7))
                                        .executes(ctx -> take(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "slot")))))
                        .then(literal("upgrade").executes(ctx -> upgrade(ctx.getSource())))));
    }

    private static int list(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("§c此命令仅玩家可用"));
            return 0;
        }
        int level = ringLevel(player);
        int size = CurseRingHelper.slotsForLevel(level);
        CurseInventorySavedData data = getData(player);
        UUID uuid = player.getUuid();

        StringBuilder sb = new StringBuilder("§6=== 随身饰品栏 ===\n");
        sb.append("§7主戒等级 §e").append(level).append(" §7→ §e").append(size).append(" §7格\n");
        for (int i = 0; i < size; i++) {
            ItemStack s = data.get(uuid, i);
            sb.append("§f[").append(i).append("] ")
                    .append(s.isEmpty() ? "§8空" : "§b" + s.getName().getString()).append("\n");
        }
        sb.append("§7/workmod:ring put <槽> 放入 | take <槽> 取出 | upgrade 升级(耗1级经验)");
        player.sendMessage(Text.literal(sb.toString()), false);
        return 1;
    }

    private static int put(ServerCommandSource source, int slot) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        int level = ringLevel(player);
        int size = CurseRingHelper.slotsForLevel(level);
        if (slot >= size) {
            source.sendError(Text.literal("§c该槽未解锁（主戒升到 " + level + " 级才有 " + size + " 格）"));
            return 0;
        }
        ItemStack held = player.getMainHandStack();
        if (!(held.getItem() instanceof CurseTrinket)) {
            source.sendError(Text.literal("§c手中不是诅咒饰品"));
            return 0;
        }
        getData(player).set(player.getUuid(), slot, held.copy(), size);
        held.decrement(1);
        source.sendFeedback(() -> Text.literal("§a已放入饰品栏 [" + slot + "]"), false);
        return 1;
    }

    private static int take(ServerCommandSource source, int slot) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        int size = CurseRingHelper.slotsForLevel(ringLevel(player));
        CurseInventorySavedData data = getData(player);
        ItemStack s = data.get(player.getUuid(), slot);
        if (s.isEmpty()) {
            source.sendError(Text.literal("§c该槽为空"));
            return 0;
        }
        data.set(player.getUuid(), slot, ItemStack.EMPTY, size);
        player.giveItemStack(s);
        source.sendFeedback(() -> Text.literal("§a已取出 " + s.getName().getString()), false);
        return 1;
    }

    private static int upgrade(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        // 在背包中找主戒
        ItemStack ring = null;
        for (ItemStack s : player.getInventory().main) {
            if (CurseRingHelper.isMainRing(s)) {
                ring = s;
                break;
            }
        }
        if (ring == null) {
            source.sendError(Text.literal("§c请把主戒「探险者之悲」放在背包中"));
            return 0;
        }
        int level = CurseRingHelper.getLevel(ring);
        if (level >= CurseRingHelper.MAX_LEVEL) {
            source.sendError(Text.literal("§c主戒已满级（" + CurseRingHelper.MAX_LEVEL + " 级，" + CurseRingHelper.MAX_SLOTS + " 格）"));
            return 0;
        }
        if (player.experienceLevel < 1) {
            source.sendError(Text.literal("§c需要 1 级经验"));
            return 0;
        }
        player.addExperienceLevels(-1);
        CurseRingHelper.setLevel(ring, level + 1);
        int newLevel = level + 1;
        source.sendFeedback(() -> Text.literal("§a主戒升至 §e" + newLevel + " §a级，饰品栏 "
                + CurseRingHelper.slotsForLevel(newLevel) + " 格"), false);
        return 1;
    }

    /** 背包中主戒的最高等级 */
    private static int ringLevel(ServerPlayerEntity player) {
        int level = 0;
        for (ItemStack s : player.getInventory().main) {
            if (CurseRingHelper.isMainRing(s)) {
                level = Math.max(level, CurseRingHelper.getLevel(s));
            }
        }
        return level;
    }

    private static CurseInventorySavedData getData(ServerPlayerEntity player) {
        return player.getServerWorld().getPersistentStateManager().getOrCreate(
                CurseInventorySavedData::fromNbt, CurseInventorySavedData::create,
                CurseInventorySavedData.getDataName());
    }
}
