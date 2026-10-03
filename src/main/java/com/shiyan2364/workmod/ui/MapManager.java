package com.shiyan2364.workmod.ui;

import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 小地图管理器
 * 管理所有标记点、迷雾探索、玩家位置
 * 按 M 键打开（客户端实现）
 */
public class MapManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final List<MapMarker> MARKERS = new ArrayList<>();
    private static final List<BlockPos> EXPLORED_CHUNKS = new ArrayList<>();

    /**
     * 添加标记
     */
    public static void addMarker(BlockPos pos, MapMarker.MarkerType type, String label) {
        MARKERS.add(new MapMarker(pos, type, label));
    }

    /**
     * 获取当前区块所有标记
     */
    public static List<MapMarker> getMarkersInRange(BlockPos center, int range) {
        List<MapMarker> result = new ArrayList<>();
        for (MapMarker marker : MARKERS) {
            if (Math.abs(marker.getPos().getX() - center.getX()) <= range &&
                Math.abs(marker.getPos().getZ() - center.getZ()) <= range) {
                result.add(marker);
            }
        }
        return result;
    }

    /**
     * 记录已探索区块
     */
    public static void markExplored(BlockPos pos) {
        BlockPos chunkPos = new BlockPos(pos.getX() >> 4, 0, pos.getZ() >> 4);
        if (!EXPLORED_CHUNKS.contains(chunkPos)) {
            EXPLORED_CHUNKS.add(chunkPos);
        }
    }

    public static boolean isExplored(BlockPos pos) {
        BlockPos chunkPos = new BlockPos(pos.getX() >> 4, 0, pos.getZ() >> 4);
        return EXPLORED_CHUNKS.contains(chunkPos);
    }

    public static List<MapMarker> getAllMarkers() { return MARKERS; }
    public static void clearMarkers() { MARKERS.clear(); }
    public static void clearExplored() { EXPLORED_CHUNKS.clear(); }
}
