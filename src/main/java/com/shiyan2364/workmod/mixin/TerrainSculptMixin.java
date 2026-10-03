package com.shiyan2364.workmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.gen.chunk.ChunkGenerator;

@Mixin(ChunkGenerator.class)
public class TerrainSculptMixin {
    // 空实现：阶段6再启用地形雕刻
}
