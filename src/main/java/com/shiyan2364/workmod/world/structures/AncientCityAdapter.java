package com.shiyan2364.workmod.world.structures;

import com.shiyan2364.workmod.api.*;
import net.minecraft.util.Identifier;
import java.util.List;

public class AncientCityAdapter implements AdventureStructure {
    @Override public Identifier id() { return Identifier.of("minecraft", "ancient_city"); }
    @Override public StructureStyle style() { return StructureStyle.MOUNTAIN; }
    @Override public StructureScale scale() { return StructureScale.LARGE; }
    @Override public List<int[]> entryPoints() { return List.of(new int[]{0,1}, new int[]{0,-1}); }
    @Override public List<String> breakTypes() { return List.of("ruin", "cliff"); }
}
