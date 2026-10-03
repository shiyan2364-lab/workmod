package com.shiyan2364.workmod.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 随身饰品栏存档（自研轻量饰品栏）
 * <p>
 * 按玩家 UUID 存槽位列表（每槽一个 ItemStack 的 NBT，空槽为空白 compound），
 * 槽数由主戒等级决定（CurseRingHelper.slotsForLevel）。
 * 持久化到世界存档 workmod_ring_inventory，离线不丢。
 */
public class CurseInventorySavedData extends PersistentState {

    private final Map<UUID, NbtList> slots = new HashMap<>();

    public static CurseInventorySavedData create() {
        return new CurseInventorySavedData();
    }

    public static CurseInventorySavedData fromNbt(NbtCompound nbt) {
        CurseInventorySavedData data = new CurseInventorySavedData();
        NbtCompound players = nbt.getCompound("players");
        for (String key : players.getKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                data.slots.put(uuid, players.getList(key, NbtElement.COMPOUND_TYPE));
            } catch (Exception ignored) {
            }
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound players = new NbtCompound();
        for (Map.Entry<UUID, NbtList> entry : slots.entrySet()) {
            players.put(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("players", players);
        return nbt;
    }

    /** 读取某槽（越界/空槽返回 EMPTY） */
    public ItemStack get(UUID uuid, int slot) {
        NbtList list = slots.get(uuid);
        if (list == null || slot < 0 || slot >= list.size()) {
            return ItemStack.EMPTY;
        }
        return ItemStack.fromNbt(list.getCompound(slot));
    }

    /** 写入某槽（空堆栈清空该槽）；size 为当前饰品栏容量，不足则补齐 */
    public void set(UUID uuid, int slot, ItemStack stack, int size) {
        NbtList list = slots.computeIfAbsent(uuid, k -> new NbtList());
        while (list.size() < size) {
            list.add(new NbtCompound());
        }
        NbtCompound entry = new NbtCompound();
        if (stack != null && !stack.isEmpty()) {
            stack.writeNbt(entry);
        }
        list.set(slot, entry);
        markDirty();
    }

    public static String getDataName() {
        return "workmod_ring_inventory";
    }
}
