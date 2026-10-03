package com.shiyan2364.workmod.event;

import com.shiyan2364.workmod.api.StructureStyle;
import com.shiyan2364.workmod.data.BreakSavedData;
import com.shiyan2364.workmod.world.road.RoadBlockTable;
import com.shiyan2364.workmod.world.road.RoadNetwork;
import com.shiyan2364.workmod.world.road.RoadSegment;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

/**
 * 断路修复监听（说明书 3️⃣：玩家修桥后永久保留）
 * <p>
 * 对断路点（RoadNetwork 登记的 break）附近右键任意方块物品：
 * - 消耗手持 1 个物品；
 * - 按路段风格恢复 3×3 路面；
 * - 路段标记 repaired，写入 BreakSavedData 存档（永久保留）。
 */
public final class RoadRepairListener {

    private RoadRepairListener() {
    }

    /** 由 Workmod.onInitialize 调用 */
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            try {
                if (world.isClient) {
                    return ActionResult.PASS;
                }
                BlockPos pos = hitResult.getBlockPos();
                RoadSegment hit = findBreakAt(world, pos);
                if (hit == null) {
                    return ActionResult.PASS;
                }
                ItemStack held = player.getStackInHand(hand);
                if (held.isEmpty()) {
                    player.sendMessage(Text.literal("§7手持任意方块物品，右键断路点即可修路（消耗 1 个）"), true);
                    return ActionResult.PASS;
                }
                repairSegment((ServerWorld) world, player, hit);
                return ActionResult.SUCCESS;
            } catch (Exception ignored) {
                return ActionResult.PASS;
            }
        });
    }

    /** 找到包含 pos 附近断路点的路段（未修复的） */
    private static RoadSegment findBreakAt(net.minecraft.world.World world, BlockPos pos) {
        for (RoadSegment seg : RoadNetwork.getInstance().getAllSegments()) {
            if (seg.isRepaired()) {
                continue;
            }
            for (BlockPos b : seg.getBreaks()) {
                if (Math.abs(b.getX() - pos.getX()) <= 2 && Math.abs(b.getZ() - pos.getZ()) <= 2) {
                    return seg;
                }
            }
        }
        return null;
    }

    private static void repairSegment(ServerWorld world, PlayerEntity player, RoadSegment seg) {
        ItemStack held = player.getMainHandStack();
        if (held.isEmpty()) {
            return;
        }
        held.decrement(1);

        RoadBlockTable.RoadStyle style;
        try {
            style = RoadBlockTable.getStyle(StructureStyle.valueOf(seg.getStyleName()));
        } catch (IllegalArgumentException e) {
            style = RoadBlockTable.getStyle(StructureStyle.VILLAGE);
        }
        // 恢复所有断路点的 3×3 路面
        for (BlockPos b : seg.getBreaks()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int bx = b.getX() + dx;
                    int bz = b.getZ() + dz;
                    int by = world.getTopY(Heightmap.Type.MOTION_BLOCKING, bx, bz);
                    world.setBlockState(new BlockPos(bx, by, bz), style.mainBlock().getDefaultState(), 3);
                }
            }
        }
        seg.setRepaired(true);

        // 永久保留：写入存档
        try {
            BreakSavedData data = world.getPersistentStateManager().getOrCreate(
                    BreakSavedData::fromNbt, BreakSavedData::create, "workmod_breaks");
            for (BlockPos b : seg.getBreaks()) {
                data.setRepaired(b, true);
            }
        } catch (Exception e) {
            // 存档失败不阻塞修路
        }
        player.sendMessage(Text.literal("§a已修复断路（" + seg.getId() + "），道路永久保留"), false);
    }
}
