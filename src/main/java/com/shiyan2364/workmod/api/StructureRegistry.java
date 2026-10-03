package com.shiyan2364.workmod.api;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class StructureRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final Map<Identifier, AdventureStructure> REGISTRY = new HashMap<>();

    public static void register(AdventureStructure structure) {
        REGISTRY.put(structure.id(), structure);
        LOGGER.info("已注册结构: {} ({})", structure.id(), structure.style().getDisplayName());
    }

    public static AdventureStructure get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static Collection<AdventureStructure> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static int size() {
        return REGISTRY.size();
    }
}
