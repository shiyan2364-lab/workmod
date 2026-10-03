package com.shiyan2364.workmod.world.breaks;

import net.minecraft.util.math.BlockPos;

/**
 * 断路点数据
 * 记录一个断路的位置/类型/修复状态
 */
public class RoadBreakPoint {
    private final BlockPos pos;
    private final RoadBreakType type;
    private boolean repaired;

    public RoadBreakPoint(BlockPos pos, RoadBreakType type) {
        this.pos = pos;
        this.type = type;
        this.repaired = false;
    }

    public BlockPos getPos() {
        return pos;
    }

    public RoadBreakType getType() {
        return type;
    }

    public boolean isRepaired() {
        return repaired;
    }

    public void setRepaired(boolean repaired) {
        this.repaired = repaired;
    }
}
