package com.shiyan2364.workmod.data;

import com.shiyan2364.workmod.item.curse.ExtremeSeries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

/**
 * 极致成就进度存档（阶段：成长主线持久化）
 * <p>
 * 原来的 ExtremeProgress 是纯内存 Map，重启世界进度就清零，
 * 直接破坏"极致成就→诅咒变强"的成长主线。本类把 8 个系列的
 * 进度值写进世界存档（workmod_extreme），由 Workmod 在
 * 世界加载时灌回 ExtremeProgress、运行中定期回写。
 */
public class ExtremeSavedData extends PersistentState {

    private final Map<String, Integer> progress = new HashMap<>();

    public static ExtremeSavedData create() {
        return new ExtremeSavedData();
    }

    public static ExtremeSavedData fromNbt(NbtCompound nbt) {
        ExtremeSavedData data = new ExtremeSavedData();
        NbtCompound tag = nbt.getCompound("progress");
        for (ExtremeSeries series : ExtremeSeries.values()) {
            int value = tag.getInt(series.name());
            if (value > 0) {
                data.progress.put(series.name(), value);
            }
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound tag = new NbtCompound();
        for (Map.Entry<String, Integer> entry : progress.entrySet()) {
            tag.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("progress", tag);
        return nbt;
    }

    /** 读取某系列进度（无记录返回 0） */
    public int get(ExtremeSeries series) {
        return progress.getOrDefault(series.name(), 0);
    }

    /** 写入某系列进度 */
    public void set(ExtremeSeries series, int value) {
        progress.put(series.name(), value);
        markDirty();
    }

    public static String getDataName() {
        return "workmod_extreme";
    }
}
