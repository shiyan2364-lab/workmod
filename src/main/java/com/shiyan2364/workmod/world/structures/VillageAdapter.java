package com.shiyan2364.workmod.world.structures;

import com.shiyan2364.workmod.api.AdventureStructure;
import com.shiyan2364.workmod.api.StructureScale;
import com.shiyan2364.workmod.api.StructureStyle;
import net.minecraft.util.Identifier;

import java.util.List;

public class VillageAdapter implements AdventureStructure {

    @Override
    public Identifier id() {
        return Identifier.of("minecraft", "village");
    }

    @Override
    public StructureStyle style() {
        return StructureStyle.VILLAGE;
    }

    @Override
    public StructureScale scale() {
        return StructureScale.MEDIUM;
    }

    @Override
    public List<int[]> entryPoints() {
        return List.of(
                new int[]{0, 1},
                new int[]{0, -1},
                new int[]{1, 0},
                new int[]{-1, 0}
        );
    }

    @Override
    public List<String> breakTypes() {
        return List.of("cliff", "vines");
    }
}
