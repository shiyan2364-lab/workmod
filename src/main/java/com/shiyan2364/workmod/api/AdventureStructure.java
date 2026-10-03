package com.shiyan2364.workmod.api;

import net.minecraft.util.Identifier;

import java.util.List;

/**
 * 冒险结构接口
 * 所有建筑群（原版/自定义）都必须实现此接口
 */
public interface AdventureStructure {

    Identifier id();

    StructureStyle style();

    StructureScale scale();

    List<int[]> entryPoints();

    List<String> breakTypes();
}
