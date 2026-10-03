package com.shiyan2364.workmod.world.breaks;

import com.shiyan2364.workmod.world.road.RoadSegment;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 断路生成器
 * 根据道路段的断路标记生成具体断路点
 */
public class BreakGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final Random RANDOM = new Random();

    /**
     * 从道路段的断路标记生成断路点列表
     */
    public List<RoadBreakPoint> generateBreaks(RoadSegment segment) {
        List<RoadBreakPoint> breaks = new ArrayList<>();

        for (BlockPos pos : segment.getBreaks()) {
            RoadBreakType type = randomBreakType(segment.getType().name());
            RoadBreakPoint breakPoint = new RoadBreakPoint(pos, type);
            breaks.add(breakPoint);
            LOGGER.info("[Workmod] 断路生成: {} @ {}", type.getDisplayName(), pos);
        }
        return breaks;
    }

    /**
     * 根据道路类型随机断路类型
     * 主路: 悬崖/遗迹 各50%
     * 支线: 悬崖/藤蔓 各50%
     * 小径: 藤蔓/篝火桥 各50%
     */
    private RoadBreakType randomBreakType(String roadTypeName) {
        return switch (roadTypeName) {
            case "MAIN" -> RANDOM.nextBoolean() ? RoadBreakType.CLIFF : RoadBreakType.RUIN;
            case "BRANCH" -> RANDOM.nextBoolean() ? RoadBreakType.CLIFF : RoadBreakType.VINES;
            case "PATH" -> RANDOM.nextBoolean() ? RoadBreakType.VINES : RoadBreakType.BRIDGE;
            default -> RoadBreakType.CLIFF;
        };
    }
}
