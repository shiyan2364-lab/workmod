package com.shiyan2364.workmod.world.canyons;

import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 峡谷生成器
 * 在指定区域生成一条大峡谷（Y=-300 ~ 1500，面积≥3000）
 */
public class CanyonGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");
    private static final Random RANDOM = new Random();
    private static final int MIN_DEPTH = 60;
    private static final int MIN_AREA = 3000;

    /**
     * 生成峡谷数据（默认位置/尺寸）
     * 实际地形生成由地形修改器（WorldGenModifier）执行
     */
    public CanyonData generate(BlockPos roughCenter) {
        // 尺寸：横×长≥3000
        int width = RANDOM.nextInt(40) + 50;      // 50~90
        int length = (MIN_AREA / width) + RANDOM.nextInt(30); // 保证面积>=3000
        int depth = RANDOM.nextInt(250) + MIN_DEPTH; // 60~310（后续可扩展到1800）

        CanyonData canyon = new CanyonData(roughCenter, width, length, depth);

        // 底部类型：普通70% / 岩浆20% / 虚空5% / 传送门5%
        int roll = RANDOM.nextInt(100);
        canyon.setBottomType(roll < 70 ? 0 : (roll < 90 ? 1 : (roll < 95 ? 2 : 3)));

        // 隐藏石阶 30%
        canyon.setHiddenPath(RANDOM.nextDouble() < 0.3);

        LOGGER.info("[Workmod] 峡谷生成 @ {} 宽{} 长{} 深{} 底类型{}",
                roughCenter, width, length, depth, canyon.getBottomType());
        return canyon;
    }

    /**
     * 检查某位置是否处于峡谷区域内
     */
    public boolean isInCanyon(BlockPos pos, CanyonData canyon) {
        int dx = Math.abs(pos.getX() - canyon.getCenter().getX());
        int dz = Math.abs(pos.getZ() - canyon.getCenter().getZ());
        return dx <= canyon.getWidth() / 2.0 && dz <= canyon.getLength() / 2.0;
    }
}
