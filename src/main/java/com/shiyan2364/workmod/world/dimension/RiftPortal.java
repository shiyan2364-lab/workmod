package com.shiyan2364.workmod.world.dimension;

import net.minecraft.util.math.BlockPos;

/**
 * 裂隙传送门点
 * 记录主世界道路尽头传送门，通向地狱/末地对应宏伟建筑旁
 * 反向归途裂隙存在于各维度建筑旁，可返回主世界
 */
public class RiftPortal {
    private final BlockPos overworldPos;   // 主世界裂隙位置
    private final WorldRealm targetRealm;  // 目标维度
    private final BlockPos targetPos;      // 目标维度坐标（建筑旁）
    private final String linkedStructureName; // 对应宏伟建筑名

    public RiftPortal(BlockPos overworldPos, WorldRealm targetRealm, BlockPos targetPos, String linkedStructureName) {
        this.overworldPos = overworldPos;
        this.targetRealm = targetRealm;
        this.targetPos = targetPos;
        this.linkedStructureName = linkedStructureName;
    }

    public BlockPos getOverworldPos() { return overworldPos; }
    public WorldRealm getTargetRealm() { return targetRealm; }
    public BlockPos getTargetPos() { return targetPos; }
    public String getLinkedStructureName() { return linkedStructureName; }
}
