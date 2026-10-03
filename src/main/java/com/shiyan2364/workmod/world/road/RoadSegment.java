package com.shiyan2364.workmod.world.road;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * 道路段数据
 * 记录一段道路的起点/终点/类型/宽度/路径点
 */
public class RoadSegment {
    private String id;
    private RoadType type;
    private BlockPos start;
    private BlockPos end;
    private int startWidth;
    private int endWidth;
    private int fadeDistance;
    private String styleName;
    private List<BlockPos> points = new ArrayList<>();
    private List<BlockPos> breaks = new ArrayList<>();
    private boolean repaired;

    public RoadSegment(String id, RoadType type, BlockPos start, BlockPos end,
                       int startWidth, int endWidth, int fadeDistance, String styleName) {
        this.id = id;
        this.type = type;
        this.start = start;
        this.end = end;
        this.startWidth = startWidth;
        this.endWidth = endWidth;
        this.fadeDistance = fadeDistance;
        this.styleName = styleName;
    }

    public String getId() { return id; }
    public RoadType getType() { return type; }
    public BlockPos getStart() { return start; }
    public BlockPos getEnd() { return end; }
    public int getStartWidth() { return startWidth; }
    public int getEndWidth() { return endWidth; }
    public int getFadeDistance() { return fadeDistance; }
    public String getStyleName() { return styleName; }
    public List<BlockPos> getPoints() { return points; }
    public List<BlockPos> getBreaks() { return breaks; }
    public boolean isRepaired() { return repaired; }
    public void setRepaired(boolean repaired) { this.repaired = repaired; }

    public void addPoint(BlockPos p) { points.add(p); }
    public void addBreak(BlockPos p) { breaks.add(p); }
}
