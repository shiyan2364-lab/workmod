package com.shiyan2364.workmod.world.dimension;

public enum WorldRealm {
    OVERWORLD("主世界", "overworld"),
    NETHER("地狱", "the_nether"),
    END("末地", "the_end");

    private final String displayName;
    private final String dimensionId;

    WorldRealm(String displayName, String dimensionId) {
        this.displayName = displayName;
        this.dimensionId = dimensionId;
    }

    public String getDisplayName() { return displayName; }
    public String getDimensionId() { return dimensionId; }
}
