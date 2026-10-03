package com.shiyan2364.workmod.world.canyons;

import com.shiyan2364.workmod.data.TerrainSavedData;
import com.shiyan2364.workmod.item.curse.ExtremeProgress;
import com.shiyan2364.workmod.item.curse.ExtremeSeries;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 峡谷自动生成器（阶段6 新增）
 * <p>
 * CanyonGenerator 只生成峡谷数据（宽/长/深/底类型），从不挖方块。
 * 本类补上"雕刻"：在玩家周围随机选点生成峡谷数据，然后分片挖出
 * V 形峡谷（边缘浅、中心深），谷底铺岩浆；每 tick 只挖
 * ROWS_PER_TICK 行，避免卡服。峡谷登记进 TerrainSavedData 存档。
 * <p>
 * 深度受世界高度限制（world.getBottomY()），配合外部高度 mod 可更深。
 */
public final class CanyonAutoGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger("workmod");

    /** 每多少 tick 启动一个新峡谷（100 tick = 5 秒） */
    private static final int TICK_INTERVAL = 100;
    /** 每 tick 雕刻的行数（性能控制） */
    private static final int ROWS_PER_TICK = 24;
    /** 距玩家的距离范围（尽量落在已加载区块内） */
    private static final int MIN_DIST = 80;
    private static final int MAX_DIST = 150;

    private static final Set<String> PROCESSED = new HashSet<>();
    private static final Random RANDOM = new Random();

    /** 当前雕刻任务 */
    private static CanyonData current = null;
    private static int halfW = 0;
    private static int halfL = 0;
    private static int row = 0;
    private static boolean carving = false;
    private static int tickCounter = 0;

    private CanyonAutoGenerator() {
    }

    /** 服务端每 tick 调用一次（由 Workmod 注册） */
    public static void tick(ServerWorld world) {
        tickCounter++;
        if (carving) {
            carveNext(world);
            return;
        }
        if (tickCounter % TICK_INTERVAL != 0) {
            return;
        }
        ServerPlayerEntity player = firstPlayer(world.getServer());
        if (player == null) {
            return;
        }
        BlockPos center = player.getBlockPos();

        // 随机选一个位置（角度 + 距离）
        double angle = RANDOM.nextDouble() * Math.PI * 2;
        int dist = MIN_DIST + RANDOM.nextInt(MAX_DIST - MIN_DIST);
        int px = center.getX() + (int) Math.round(Math.cos(angle) * dist);
        int pz = center.getZ() + (int) Math.round(Math.sin(angle) * dist);
        int py = world.getTopY(Heightmap.Type.MOTION_BLOCKING, px, pz);
        String key = (px >> 4) + "," + (pz >> 4);
        if (PROCESSED.contains(key)) {
            return;
        }

        CanyonData canyon = new CanyonGenerator().generate(new BlockPos(px, py, pz));
        if (canyon == null || !canyon.isValid()) {
            PROCESSED.add(key);
            return;
        }
        PROCESSED.add(key);

        // 存档
        try {
            TerrainSavedData terrain = world.getPersistentStateManager().getOrCreate(
                    TerrainSavedData::fromNbt, TerrainSavedData::create, "workmod_terrain");
            terrain.addCanyon(canyon);
        } catch (Exception e) {
            LOGGER.warn("[Workmod] 峡谷存档失败: {}", e.getMessage());
        }

        // 启动雕刻任务
        current = canyon;
        halfW = Math.max(2, canyon.getWidth() / 2);
        halfL = Math.max(2, canyon.getLength() / 2);
        row = 0;
        carving = true;
        LOGGER.info("[Workmod] 峡谷开始雕刻: {} 宽{} 长{} 深{}",
                canyon.getCenter(), canyon.getWidth(), canyon.getLength(), canyon.getDepth());
    }

    /** 世界重载时重置 */
    public static void reset() {
        current = null;
        carving = false;
        row = 0;
        PROCESSED.clear();
    }

    // ------------------------------------------------------------------
    // 雕刻
    // ------------------------------------------------------------------

    private static void carveNext(ServerWorld world) {
        if (current == null) {
            carving = false;
            return;
        }
        BlockPos c = current.getCenter();
        int bottom = Math.max(world.getBottomY(), c.getY() - current.getDepth());
        int rowsDone = 0;

        while (rowsDone < ROWS_PER_TICK && row <= halfL * 2) {
            int z = c.getZ() - halfL + row;
            carveRow(world, c, z, bottom);
            row++;
            rowsDone++;
        }
        if (row > halfL * 2) {
            LOGGER.info("[Workmod] 峡谷雕刻完成: {} 深 {}", current.getCenter(), current.getDepth());
            // 极致成就·深渊：雕刻完成一条峡谷即推进"潜入峡谷"
            ExtremeProgress.addProgress(ExtremeSeries.ABYSS);
            current = null;
            carving = false;
            row = 0;
        }
    }

    /** 挖一行：圆形 V 形截面，边缘浅、中心深，谷底铺岩浆 */
    private static void carveRow(ServerWorld world, BlockPos c, int z, int bottom) {
        int zOff = z - c.getZ();
        double nz = (double) zOff / halfL;
        for (int x = c.getX() - halfW; x <= c.getX() + halfW; x++) {
            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            double nx = (double) (x - c.getX()) / halfW;
            double d = Math.sqrt(nx * nx + nz * nz);
            if (d > 1) {
                continue;
            }
            int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
            int depthAt = (int) (current.getDepth() * (1 - d));
            int from = Math.max(bottom, topY - depthAt);
            // bottomType：0=普通(留空) 1=岩浆 2=虚空(留空) 3=下界传送门(暂留空)
            boolean lavaBottom = current.getBottomType() == 1;
            for (int y = topY; y >= from; y--) {
                BlockPos p = new BlockPos(x, y, z);
                if (y == from) {
                    if (lavaBottom) {
                        world.setBlockState(p, Blocks.LAVA.getDefaultState(), 2);
                    }
                } else {
                    world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                }
            }
        }
    }

    private static ServerPlayerEntity firstPlayer(MinecraftServer server) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        return players.isEmpty() ? null : players.get(0);
    }
}
