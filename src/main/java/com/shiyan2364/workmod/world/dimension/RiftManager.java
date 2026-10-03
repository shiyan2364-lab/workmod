package com.shiyan2364.workmod.world.dimension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 裂隙管理
 * 注册/查找三界间的连通点
 * 连贯性：主世界裂隙 -> 目标维度建筑旁
 */
public class RiftManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final List<RiftPortal> PORTALS = new ArrayList<>();

    public static void registerPortal(RiftPortal portal) {
        Optional<RiftPortal> existing = PORTALS.stream()
                .filter(p -> p.getOverworldPos().equals(portal.getOverworldPos()))
                .findFirst();
        if (existing.isEmpty()) {
            PORTALS.add(portal);
            LOGGER.info("[Workmod] 裂隙注册: 主世界{} -> {} {}",
                    portal.getOverworldPos(),
                    portal.getTargetRealm().getDisplayName(),
                    portal.getTargetPos());
        }
    }

    public static List<RiftPortal> getAllPortals() {
        return List.copyOf(PORTALS);
    }

    public static void clearAll() {
        PORTALS.clear();
    }
}
