package com.shiyan2364.workmod.item.curse;

import java.util.HashMap;
import java.util.Map;

public class CurseItem {

    public enum CurseType {
        MAIN_RING("探险者之悲", null),
        GREED_HAND("贪婪之手", ExtremeSeries.ABYSS),
        ENVY_EYE("嫉妒之眼", ExtremeSeries.STAR),
        WRATH_HAMMER("暴怒之锤", ExtremeSeries.ASH),
        SLOTH_STONE("怠惰之石", ExtremeSeries.TIME),
        PRIDE_CROWN("傲慢之冠", ExtremeSeries.SKY),
        LUST_FLOWER("色欲之花", ExtremeSeries.LIFE),
        FEAR_HEART("恐惧之心", ExtremeSeries.TIDE);

        private final String displayName;
        private final ExtremeSeries boundSeries;

        CurseType(String displayName, ExtremeSeries boundSeries) {
            this.displayName = displayName;
            this.boundSeries = boundSeries;
        }

        public String getDisplayName() { return displayName; }
        public ExtremeSeries getBoundSeries() { return boundSeries; }
    }

    private static final Map<CurseType, CurseState> STATES = new HashMap<>();

    private static class CurseState {
        int customStrength = 0;
        boolean manualMode = false;
    }

    static {
        for (CurseType type : CurseType.values()) {
            STATES.put(type, new CurseState());
        }
    }

    public static int getStrength(CurseType type) {
        CurseState state = STATES.get(type);
        if (state.manualMode) return state.customStrength;
        if (type.getBoundSeries() == null) return getMainRingDefaultStrength();
        return ExtremeProgress.getDefaultStrength(type.getBoundSeries());
    }

    public static void setCustomStrength(CurseType type, int strength) {
        CurseState state = STATES.get(type);
        state.customStrength = strength;
        state.manualMode = true;
    }

    public static void resetToDefault(CurseType type) {
        CurseState state = STATES.get(type);
        state.customStrength = 0;
        state.manualMode = false;
    }

    public static boolean isManualMode(CurseType type) {
        return STATES.get(type).manualMode;
    }

    private static int getMainRingDefaultStrength() {
        int total = 0;
        int count = ExtremeSeries.values().length;
        for (ExtremeSeries series : ExtremeSeries.values()) {
            total += ExtremeProgress.getDefaultStrength(series);
        }
        return total / count;
    }

    public static void resetAll() {
        for (CurseType type : CurseType.values()) {
            resetToDefault(type);
        }
        ExtremeProgress.reset();
    }
}
