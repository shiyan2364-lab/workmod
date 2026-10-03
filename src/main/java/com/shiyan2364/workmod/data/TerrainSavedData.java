package com.shiyan2364.workmod.data;

import com.shiyan2364.workmod.world.canyons.CanyonData;
import com.shiyan2364.workmod.world.mountains.MountainTrailData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

public class TerrainSavedData extends PersistentState {
    private static final String DATA_NAME = "workmod_terrain";

    private static final List<BlockPos> cachedCanyons = new ArrayList<>();
    private static final List<BlockPos> cachedTrails = new ArrayList<>();

    private final List<CanyonData> canyons = new ArrayList<>();
    private final List<MountainTrailData> trails = new ArrayList<>();

    public static TerrainSavedData create() {
        return new TerrainSavedData();
    }


    public static TerrainSavedData fromNbt(NbtCompound nbt) {
        TerrainSavedData data = new TerrainSavedData();
        NbtList canyonList = nbt.getList("Canyons", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < canyonList.size(); i++) {
            NbtCompound comp = canyonList.getCompound(i);
            BlockPos center = new BlockPos(comp.getInt("CX"), comp.getInt("CY"), comp.getInt("CZ"));
            CanyonData canyon = new CanyonData(center, comp.getInt("W"), comp.getInt("L"), comp.getInt("D"));
            canyon.setBottomType(comp.getInt("BT"));
            canyon.setHiddenPath(comp.getBoolean("HP"));
            canyon.setDiscovered(comp.getBoolean("Disc"));
            data.canyons.add(canyon);
            cachedCanyons.add(center);
        }

        NbtList trailList = nbt.getList("Trails", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < trailList.size(); i++) {
            NbtCompound comp = trailList.getCompound(i);
            BlockPos foot = new BlockPos(comp.getInt("FX"), comp.getInt("FY"), comp.getInt("FZ"));
            BlockPos summit = new BlockPos(comp.getInt("SX"), comp.getInt("SY"), comp.getInt("SZ"));
            MountainTrailData trail = new MountainTrailData(foot, summit, comp.getString("Name"));
            trail.setPathWidth(comp.getInt("PW"));
            trail.setTreasure(comp.getBoolean("HT"), comp.getInt("TR"));
            trail.setRewardClaimed(comp.getBoolean("RC"));
            trail.setSummitReached(comp.getBoolean("SR"));
            data.trails.add(trail);
            cachedTrails.add(summit);
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList canyonList = new NbtList();
        for (CanyonData canyon : canyons) {
            NbtCompound comp = new NbtCompound();
            comp.putInt("CX", canyon.getCenter().getX());
            comp.putInt("CY", canyon.getCenter().getY());
            comp.putInt("CZ", canyon.getCenter().getZ());
            comp.putInt("W", canyon.getWidth());
            comp.putInt("L", canyon.getLength());
            comp.putInt("D", canyon.getDepth());
            comp.putInt("BT", canyon.getBottomType());
            comp.putBoolean("HP", canyon.hasHiddenPath());
            comp.putBoolean("Disc", canyon.isDiscovered());
            canyonList.add(comp);
        }
        nbt.put("Canyons", canyonList);

        NbtList trailList = new NbtList();
        for (MountainTrailData trail : trails) {
            NbtCompound comp = new NbtCompound();
            comp.putInt("FX", trail.getFoot().getX());
            comp.putInt("FY", trail.getFoot().getY());
            comp.putInt("FZ", trail.getFoot().getZ());
            comp.putInt("SX", trail.getSummit().getX());
            comp.putInt("SY", trail.getSummit().getY());
            comp.putInt("SZ", trail.getSummit().getZ());
            comp.putString("Name", trail.getName());
            comp.putInt("PW", trail.getPathWidth());
            comp.putBoolean("HT", trail.hasTreasure());
            comp.putInt("TR", trail.getTreasureRarity());
            comp.putBoolean("RC", trail.isRewardClaimed());
            comp.putBoolean("SR", trail.isSummitReached());
            trailList.add(comp);
        }
        nbt.put("Trails", trailList);
        return nbt;
    }

    public void addCanyon(CanyonData canyon) {
        canyons.add(canyon);
        cachedCanyons.add(canyon.getCenter());
        markDirty();
    }

    public void addTrail(MountainTrailData trail) {
        trails.add(trail);
        cachedTrails.add(trail.getSummit());
        markDirty();
    }

    public List<CanyonData> getAllCanyons() { return canyons; }
    public List<MountainTrailData> getAllTrails() { return trails; }

    // 罗盘用
    public static List<BlockPos> getAllCanyonsPositions() { return cachedCanyons; }
    public static List<BlockPos> getAllTrailsPositions() { return cachedTrails; }

    public static String getDataName() { return DATA_NAME; }
}
