package com.shiyan2364.workmod.world.structures;

import com.shiyan2364.workmod.api.*;
import net.minecraft.util.Identifier;
import java.util.List;

public class EndCityAdapter implements AdventureStructure {
    @Override public Identifier id() { return Identifier.of("minecraft", "end_city"); }
    @Override public StructureStyle style() { return StructureStyle.RUIN; }
    @Override public StructureScale scale() { return StructureScale.LARGE; }
    @Override public List<int[]> entryPoints() { return List.of(new int[]{0,1}); }
    @Override public List<String> breakTypes() { return List.of("ruin", "bridge"); }
}
