package com.shiyan2364.workmod.world.mountains;

import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 高山登山道生成器
 * 之字形折返，1~2格宽，无休息平台
 */
public class MountainTrailGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final Random RANDOM = new Random();

    /**
     * 生成登山道数据
     * @param foot 山脚入口
     * @param summit 山顶
     * @param name 山峰名
     */
    public MountainTrailData generate(BlockPos foot, BlockPos summit, String name) {
        MountainTrailData trail = new MountainTrailData(foot, summit, name);

        // 宽度：80% 1格 / 20% 2格
        trail.setPathWidth(RANDOM.nextDouble() < 0.8 ? 1 : 2);

        // 生成之字形路径点
        generateSwitchbackPath(trail);

        // 山顶奖励：20%有宝箱
        if (RANDOM.nextDouble() < 0.2) {
            int rarity = RANDOM.nextInt(3); // 0=普通 1=稀有 2=传说（5%:8%:7%约）
            trail.setTreasure(true, rarity);
        }

        LOGGER.info("[Workmod] 登山道生成: {} 起点{} 山顶{} 宽{}", name, foot, summit, trail.getPathWidth());
        return trail;
    }

    /**
     * 之字形路径：逐步上升，路径点之间高度递增
     */
    private void generateSwitchbackPath(MountainTrailData trail) {
        BlockPos foot = trail.getFoot();
        BlockPos summit = trail.getSummit();

        int dx = summit.getX() - foot.getX();
        int dz = summit.getZ() - foot.getZ();
        int dy = summit.getY() - foot.getY();

        int segments = Math.max(4, Math.abs(dy) / 30); // 每30格高度一个折返
        for (int i = 0; i <= segments; i++) {
            double t1 = (double) i / segments;
            double t2 = (double) (i + 1) / segments;

            int x = (int) Math.round(foot.getX() + dx * t1);
            int z = (int) Math.round(foot.getZ() + dz * t1);
            int y = (int) Math.round(foot.getY() + dy * t1);

            // 奇数段向西偏移，偶数段向东偏移（之字形）
            int offset = (i % 2 == 0) ? 3 : -3;
            trail.addPathPoint(new BlockPos(x + offset, y, z + offset));
        }

        trail.addPathPoint(summit);
    }
}
