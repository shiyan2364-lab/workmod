package com.shiyan2364.workmod.world.structures;

import com.shiyan2364.workmod.api.*;
import net.minecraft.util.Identifier;
import java.util.List;

public class RuinedPortalNetherAdapter implements AdventureStructure {
    @Override public Identifier id() { return Identifier.of("minecraft", "ruined_portal"); }
    @Override public StructureStyle style() { return StructureStyle.RUIN; }
    @Override public StructureScale scale() { return StructureScale.TINY; }
    @Override public List<int[]> entryPoints() { return List.of(new int[]{0,1}); }
    @Override public List<String> breakTypes() { return List.of("cliff", "bridge"); }
}
