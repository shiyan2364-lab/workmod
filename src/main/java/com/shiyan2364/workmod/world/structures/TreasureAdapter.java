package com.shiyan2364.workmod.world.structures;

import com.shiyan2364.workmod.api.*;
import net.minecraft.util.Identifier;
import java.util.List;

public class TreasureAdapter implements AdventureStructure {
    @Override public Identifier id() { return Identifier.of("minecraft", "buried_treasure"); }
    @Override public StructureStyle style() { return StructureStyle.OCEAN; }
    @Override public StructureScale scale() { return StructureScale.TINY; }
    @Override public List<int[]> entryPoints() { return List.of(new int[]{0,1}); }
    @Override public List<String> breakTypes() { return List.of("cliff"); }
}
