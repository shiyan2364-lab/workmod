package com.shiyan2364.workmod.world.dimension;

import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 裂隙门结构放置器
 * 在主世界道路尽头/归途点放置一个明显的"裂隙门"：
 * - 门框：黑曜石（5格高，3格宽）
 * - 门内传送方块：末地传送门框架（下方）+ 海晶灯（发光）
 * - 底部地面：磨制黑石
 *
 * 玩家站到门内传送方块上 → RiftTeleportHandler 检测并传送
 */
public class RiftStructurePlacer {
    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");

    /**
     * 放置裂隙门（朝南，尺寸约 3x5x3）
     * @param world 主世界
     * @param pos 门中心底部
     */
    public static void placeRift(ServerWorld world, BlockPos pos, boolean isReturnGate) {
        if (world.isClient) return;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        // 门框（5格高，3格宽，黑曜石）
        for (int dz = -1; dz <= 1; dz++) {
            for (int dy = 0; dy <= 4; dy++) {
                world.setBlockState(new BlockPos(x + dz, y + dy, z), Blocks.OBSIDIAN.getDefaultState());
            }
        }
        // 顶部横梁
        for (int dx = -1; dx <= 1; dx++) {
            world.setBlockState(new BlockPos(x + dx, y + 4, z), Blocks.OBSIDIAN.getDefaultState());
        }

        // 门内部（y=1 放传送方块，y=2 放发光海晶灯）
        world.setBlockState(new BlockPos(x, y + 1, z), Blocks.END_PORTAL_FRAME.getDefaultState());
        world.setBlockState(new BlockPos(x, y + 2, z), Blocks.SEA_LANTERN.getDefaultState());
        // 内部上方放岩浆（视觉上像裂隙）
        world.setBlockState(new BlockPos(x, y + 3, z), Blocks.LAVA.getDefaultState());

        // 底部地面（磨制黑石平台）
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.setBlockState(new BlockPos(x + dx, y - 1, z + dz), Blocks.POLISHED_BLACKSTONE.getDefaultState());
            }
        }

        // 归途门额外加个"路标"（告示牌）标记
        if (isReturnGate) {
            world.setBlockState(new BlockPos(x, y + 3, z + 1), Blocks.OAK_SIGN.getDefaultState());
        }

        LOGGER.info("[Workmod] 裂隙门放置 @ {} {} {}", x, y, z);
    }
}
