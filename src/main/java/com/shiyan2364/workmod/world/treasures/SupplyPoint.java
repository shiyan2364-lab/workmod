package com.shiyan2364.workmod.world.treasures;

import net.minecraft.util.math.BlockPos;

/**
 * 补给点数据
 * 生成在道路旁/峡谷边缘/高山山脚，提供基础补给
 * 单次拾取后永久消失（存档标记）
 */
public class SupplyPoint {
    private final BlockPos pos;
    private final SupplyType type;
    private boolean collected;

    public enum SupplyType {
        FOOD("食物", "面包×8 + 苹果×3"),
        TOOLS("工具", "铁镐 + 火把×32"),
        BLOCKS("方块", "石头×64 + 木板×32"),
        WATER("水", "水桶×2"),
        MIXED("综合", "随机2-3项补给");

        private final String displayName;
        private final String content;

        SupplyType(String displayName, String content) {
            this.displayName = displayName;
            this.content = content;
        }

        public String getDisplayName() { return displayName; }
        public String getContent() { return content; }
    }

    public SupplyPoint(BlockPos pos, SupplyType type) {
        this.pos = pos;
        this.type = type;
        this.collected = false;
    }

    public BlockPos getPos() { return pos; }
    public SupplyType getType() { return type; }
    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }
}
