package com.shiyan2364.workmod.world.mountains;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * 高山登山道数据
 * 记录一条登山道的起点/终点/路径点/奖励状态
 */
public class MountainTrailData {
    private final BlockPos foot;        // 山脚入口（石碑处）
    private final BlockPos summit;      // 山顶
    private final String name;          // 山峰名
    private final List<BlockPos> path = new ArrayList<>(); // 路径点
    private int pathWidth = 1;          // 80% 1格 / 20% 2格
    private boolean hasTreasure;        // 20%有宝箱
    private int treasureRarity;         // 0=普通 1=稀有 2=传说
    private boolean rewardClaimed;      // 是否已领取
    private boolean summitReached;      // 是否登顶过

    public MountainTrailData(BlockPos foot, BlockPos summit, String name) {
        this.foot = foot;
        this.summit = summit;
        this.name = name;
    }

    public BlockPos getFoot() { return foot; }
    public BlockPos getSummit() { return summit; }
    public String getName() { return name; }
    public List<BlockPos> getPath() { return path; }
    public int getPathWidth() { return pathWidth; }
    public boolean hasTreasure() { return hasTreasure; }
    public int getTreasureRarity() { return treasureRarity; }
    public boolean isRewardClaimed() { return rewardClaimed; }
    public boolean isSummitReached() { return summitReached; }

    public void setPathWidth(int pathWidth) { this.pathWidth = pathWidth; }
    public void setTreasure(boolean hasTreasure, int treasureRarity) {
        this.hasTreasure = hasTreasure;
        this.treasureRarity = treasureRarity;
    }
    public void setRewardClaimed(boolean rewardClaimed) { this.rewardClaimed = rewardClaimed; }
    public void setSummitReached(boolean summitReached) { this.summitReached = summitReached; }

    public void addPathPoint(BlockPos p) { path.add(p); }
}
