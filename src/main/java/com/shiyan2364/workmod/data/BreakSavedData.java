package com.shiyan2364.workmod.data;

import com.shiyan2364.workmod.world.breaks.RoadBreakPoint;
import com.shiyan2364.workmod.world.breaks.RoadBreakType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

/**
 * 断路存档
 * 记录所有断路点的修复状态，玩家修桥后永久保存
 */
public class BreakSavedData extends PersistentState {
    private static final String DATA_NAME = "workmod_breaks";
    private final List<RoadBreakPoint> breaks = new ArrayList<>();

    public static BreakSavedData create() {
        return new BreakSavedData();
    }


    public static BreakSavedData fromNbt(NbtCompound nbt) {
        BreakSavedData data = new BreakSavedData();
        NbtList list = nbt.getList("Breaks", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound comp = list.getCompound(i);
            BlockPos pos = new BlockPos(
                    comp.getInt("X"),
                    comp.getInt("Y"),
                    comp.getInt("Z")
            );
            RoadBreakType type = RoadBreakType.values()[comp.getInt("Type")];
            RoadBreakPoint point = new RoadBreakPoint(pos, type);
            point.setRepaired(comp.getBoolean("Repaired"));
            data.breaks.add(point);
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (RoadBreakPoint point : breaks) {
            NbtCompound comp = new NbtCompound();
            comp.putInt("X", point.getPos().getX());
            comp.putInt("Y", point.getPos().getY());
            comp.putInt("Z", point.getPos().getZ());
            comp.putInt("Type", point.getType().ordinal());
            comp.putBoolean("Repaired", point.isRepaired());
            list.add(comp);
        }
        nbt.put("Breaks", list);
        return nbt;
    }

    public void addBreak(RoadBreakPoint point) {
        breaks.add(point);
        markDirty();
    }

    public List<RoadBreakPoint> getAllBreaks() {
        return breaks;
    }

    public void setRepaired(BlockPos pos, boolean repaired) {
        for (RoadBreakPoint point : breaks) {
            if (point.getPos().equals(pos)) {
                point.setRepaired(repaired);
                markDirty();
                return;
            }
        }
    }

    public static String getDataName() {
        return DATA_NAME;
    }
}
