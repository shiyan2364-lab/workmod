package com.shiyan2364.workmod;

import com.shiyan2364.workmod.api.StructureRegistry;
import com.shiyan2364.workmod.command.CurseCommand;
import com.shiyan2364.workmod.command.CurseRingCommand;
import com.shiyan2364.workmod.command.GenRoadCommand;
import com.shiyan2364.workmod.command.TradeCommand;
import com.shiyan2364.workmod.command.WorkmodCommand;
import com.shiyan2364.workmod.data.BreakSavedData;
import com.shiyan2364.workmod.data.DifficultySavedData;
import com.shiyan2364.workmod.data.ExtremeSavedData;
import com.shiyan2364.workmod.data.TerrainSavedData;
import com.shiyan2364.workmod.data.TreasureSavedData;
import com.shiyan2364.workmod.event.DeathListener;
import com.shiyan2364.workmod.event.CurseDropListener;
import com.shiyan2364.workmod.event.RoadRepairListener;
import com.shiyan2364.workmod.item.CurseItems;
import com.shiyan2364.workmod.item.Handbook;
import com.shiyan2364.workmod.item.ModItems;
import com.shiyan2364.workmod.item.TradeBook;
import com.shiyan2364.workmod.item.compat.TrinketCompat;
import com.shiyan2364.workmod.item.curse.ExtremeProgress;
import com.shiyan2364.workmod.item.curse.ExtremeSeries;
import com.shiyan2364.workmod.world.dimension.RiftAutoGenerator;
import com.shiyan2364.workmod.world.dimension.RiftCommand;
import com.shiyan2364.workmod.world.dimension.RiftRewardGenerator;
import com.shiyan2364.workmod.world.dimension.RiftTeleportHandler;
import com.shiyan2364.workmod.world.monsters.GuardAutoSpawner;
import com.shiyan2364.workmod.world.monsters.GuardVisualHandler;
import com.shiyan2364.workmod.world.monsters.RealTimeDifficulty;
import com.shiyan2364.workmod.world.canyons.CanyonAutoGenerator;
import com.shiyan2364.workmod.world.mountains.MountainAutoGenerator;
import com.shiyan2364.workmod.world.road.RoadAutoGenerator;
import com.shiyan2364.workmod.world.structures.AncientCityAdapter;
import com.shiyan2364.workmod.world.structures.BastionAdapter;
import com.shiyan2364.workmod.world.structures.EndCityAdapter;
import com.shiyan2364.workmod.world.structures.EndGatewayAdapter;
import com.shiyan2364.workmod.world.structures.FortressAdapter;
import com.shiyan2364.workmod.world.structures.NetherFortressAdapter;
import com.shiyan2364.workmod.world.structures.RuinedPortalAdapter;
import com.shiyan2364.workmod.world.structures.RuinedPortalNetherAdapter;
import com.shiyan2364.workmod.world.structures.TreasureAdapter;
import com.shiyan2364.workmod.world.structures.VillageAdapter;
import com.shiyan2364.workmod.world.treasures.TreasureAutoSpawner;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workmod 主入口（阶段1~6 + 诅咒/成就接线版）
 * <p>
 * 保留原有全部接线：物品、命令（WorkmodCommand）、死亡惩罚、
 * 10 个结构注册、四份存档加载、实时难度 tick。
 * <p>
 * 各阶段接线：
 * - 阶段1  结构→道路 自动铺路（RoadAutoGenerator）
 * - 阶段1b 出生发《冒险者手册》（ServerPlayConnectionEvents.JOIN）
 * - 阶段2  断路（RoadGenerator.applyBreaks：断路点生成 + 挖断路面）
 * - 阶段3  补给/宝藏（TreasureAutoSpawner）
 * - 阶段4  守卫（GuardAutoSpawner 沿道路生成 + GuardVisualHandler 血条）
 * - 阶段5  裂隙（RiftAutoGenerator 道路尽头放门 + RiftTeleportHandler 传送）
 * - 阶段6  峡谷/高山雕刻（CanyonAutoGenerator / MountainAutoGenerator）
 * - 诅咒  诅咒饰品物品化（CurseItems + 击杀掉落 CurseDropListener）
 * - 成就  极致成就游戏内触发（登顶/探谷/时痕/岩魂/维度/海底）
 */
public class Workmod implements ModInitializer {

    public static final String MOD_ID = "workmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** 宝藏/补给扫描节流（每 100 tick 一次） */
    private static int treasureTick = 0;
    /** 极致成就·时痕 节流（每 6000 tick = 5 分钟 +1） */
    private static int timeTick = 0;
    /** 极致成就·维度/水域 节流（每 1200 tick 扫描一次在线玩家） */
    private static int dimensionTick = 0;
    /** 极致成就·岩魂 挖石头计数 */
    private static int rockBroken = 0;
    /** 极致成就·生机 计数（种植/喂养每 10 次 +1） */
    private static int lifeActions = 0;
    /** 极致成就存档回写节流（每 600 tick = 30 秒） */
    private static int extremeSyncTick = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("[Workmod] 冒险路网模组初始化中...");

        // 物品与命令
        ModItems.registerAll();
        CurseItems.registerAll();
        TrinketCompat.registerAll();
        WorkmodCommand.register();
        GenRoadCommand.register();
        RiftCommand.register();
        TradeCommand.register();
        CurseCommand.register();
        CurseRingCommand.register();
        DeathListener.register();
        CurseDropListener.register();
        RoadRepairListener.register();

        // 极致成就·岩魂：挖石头类方块推进（每 50 块 +1）
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            try {
                if (world.isClient) {
                    return;
                }
                Block b = state.getBlock();
                if (b == Blocks.STONE || b == Blocks.DEEPSLATE || b == Blocks.COBBLESTONE
                        || b == Blocks.DIORITE || b == Blocks.GRANITE || b == Blocks.ANDESITE) {
                    if (++rockBroken % 50 == 0) {
                        ExtremeProgress.addProgress(ExtremeSeries.ROCK);
                    }
                }
            } catch (Exception ignored) {
            }
        });

        // 极致成就·生机：种植（树苗/种子）与喂养动物推进（每 10 次 +1）
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            try {
                if (!world.isClient && isPlantable(player.getStackInHand(hand))
                        && ++lifeActions % 10 == 0) {
                    ExtremeProgress.addProgress(ExtremeSeries.LIFE);
                }
            } catch (Exception ignored) {
            }
            return ActionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            try {
                if (!world.isClient && entity instanceof AnimalEntity
                        && isAnimalFood(player.getStackInHand(hand))
                        && ++lifeActions % 10 == 0) {
                    ExtremeProgress.addProgress(ExtremeSeries.LIFE);
                }
            } catch (Exception ignored) {
            }
            return ActionResult.PASS;
        });

        // 结构注册
        registerStructures();
        LOGGER.info("[Workmod] 已注册 {} 个结构", StructureRegistry.size());

        // 世界加载：加载存档 + 重置各生成器记录
        ServerWorldEvents.LOAD.register(Workmod::onWorldLoad);
        // 服务端 tick：难度衰减 + 各阶段自动生成
        ServerTickEvents.END_SERVER_TICK.register(Workmod::onEndTick);

        // 阶段1b：新玩家发《冒险者手册》+《旅者商册》
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (!hasHandbook(player)) {
                player.giveItemStack(Handbook.create());
            }
            if (!hasTradeBook(player)) {
                player.giveItemStack(TradeBook.create());
            }
            LOGGER.info("[Workmod] 向 {} 发放手册与商册", player.getName().getString());
        });
        // 阶段5：玩家下线清理传送记录
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                RiftTeleportHandler.onPlayerDisconnect(handler.getPlayer().getUuid()));

        LOGGER.info("[Workmod] 初始化完成！");
    }

    private static void registerStructures() {
        StructureRegistry.register(new VillageAdapter());
        StructureRegistry.register(new BastionAdapter());
        StructureRegistry.register(new FortressAdapter());
        StructureRegistry.register(new AncientCityAdapter());
        StructureRegistry.register(new EndCityAdapter());
        StructureRegistry.register(new EndGatewayAdapter());
        StructureRegistry.register(new NetherFortressAdapter());
        StructureRegistry.register(new RuinedPortalAdapter());
        StructureRegistry.register(new RuinedPortalNetherAdapter());
        StructureRegistry.register(new TreasureAdapter());
    }

    /** 世界加载：读取四份 PersistentState 存档并加载难度 */
    private static void onWorldLoad(MinecraftServer server, ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();
        manager.getOrCreate(BreakSavedData::fromNbt, BreakSavedData::create, "workmod_breaks");
        DifficultySavedData diffData = manager.getOrCreate(
                DifficultySavedData::fromNbt, DifficultySavedData::create, "workmod_difficulty");
        manager.getOrCreate(TerrainSavedData::fromNbt, TerrainSavedData::create, "workmod_terrain");
        manager.getOrCreate(TreasureSavedData::fromNbt, TreasureSavedData::create, "workmod_treasures");
        RealTimeDifficulty.getInstance().loadFromData(diffData);

        // 极致成就进度：从存档灌回内存（防重启清零）
        ExtremeSavedData extremeData = manager.getOrCreate(
                ExtremeSavedData::fromNbt, ExtremeSavedData::create, "workmod_extreme");
        ExtremeProgress.reset();
        for (ExtremeSeries series : ExtremeSeries.values()) {
            int count = extremeData.get(series);
            for (int i = 0; i < count; i++) {
                ExtremeProgress.addProgress(series);
            }
        }

        // 重置各生成器处理记录
        RoadAutoGenerator.reset();
        GuardAutoSpawner.resetTracking();
        TreasureAutoSpawner.resetTracking();
        RiftAutoGenerator.resetTracking();
        RiftRewardGenerator.reset();
        CanyonAutoGenerator.reset();
        MountainAutoGenerator.reset();
        LOGGER.info("[Workmod] 存档数据已加载");
    }

    /** 服务端 tick：难度衰减 + 各阶段自动生成 */
    private static void onEndTick(MinecraftServer server) {
        try {
            RealTimeDifficulty difficulty = RealTimeDifficulty.getInstance();
            difficulty.tick(server.getTicks());

            ServerWorld world = server.getOverworld();
            if (world != null) {
                DifficultySavedData diffData = world.getPersistentStateManager().getOrCreate(
                        DifficultySavedData::fromNbt, DifficultySavedData::create, "workmod_difficulty");
                difficulty.saveToData(diffData);
            }

            // 阶段1：结构 → 道路 自动铺路
            RoadAutoGenerator.tick(server);

            // 阶段4：守卫（沿道路）+ 血条
            GuardAutoSpawner.tick(server);
            if (world != null) {
                GuardVisualHandler.tick(world);
            }

            // 阶段3：补给/宝藏（沿道路，节流）
            if (world != null && ++treasureTick % 100 == 0) {
                TreasureAutoSpawner.tick(world);
            }

            // 阶段5：裂隙自动门 + 传送
            RiftAutoGenerator.tick(server);
            RiftTeleportHandler.tick(server);

            // 裂隙奖励箱（含诅咒饰品）
            RiftRewardGenerator.tick(server);

            // 阶段6：峡谷 / 高山登山道雕刻（主世界叠加雕刻）
            if (world != null) {
                CanyonAutoGenerator.tick(world);
                MountainAutoGenerator.tick(world);
            }

            // 极致成就·时痕：每 5 分钟 +1（时间积累）
            if (++timeTick % 6000 == 0) {
                ExtremeProgress.addProgress(ExtremeSeries.TIME);
            }

            // 极致成就进度回写存档（每 30 秒），保证重启不丢
            if (++extremeSyncTick % 600 == 0) {
                ServerWorld ow = server.getOverworld();
                if (ow != null) {
                    ExtremeSavedData extremeData = ow.getPersistentStateManager().getOrCreate(
                            ExtremeSavedData::fromNbt, ExtremeSavedData::create, "workmod_extreme");
                    for (ExtremeSeries series : ExtremeSeries.values()) {
                        extremeData.set(series, ExtremeProgress.getProgress(series));
                    }
                }
            }

            // 极致成就·维度：把在线玩家当探测（下界/末地按原版维度判定）+ 沧浪海底
            if (++dimensionTick % 1200 == 0) {
                for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                    RegistryKey<World> dim = p.getWorld().getRegistryKey();
                    if (dim == World.NETHER) {
                        ExtremeProgress.addProgress(ExtremeSeries.ASH);
                    } else if (dim == World.END) {
                        ExtremeProgress.addProgress(ExtremeSeries.STAR);
                    }
                    if (world != null && dim == World.OVERWORLD) {
                        double feet = p.getY();
                        if (feet < world.getSeaLevel() - 8) {
                            ExtremeProgress.addProgress(ExtremeSeries.TIDE);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[Workmod] tick 异常: {}", e.getMessage());
        }
    }

    private static boolean hasHandbook(ServerPlayerEntity player) {
        for (ItemStack stack : player.getInventory().main) {
            if (Handbook.isHandbook(stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTradeBook(ServerPlayerEntity player) {
        for (ItemStack stack : player.getInventory().main) {
            if (TradeBook.isTradeBook(stack)) {
                return true;
            }
        }
        return false;
    }

    /** 判断是否为可种植物品（树苗/种子，按 id 后缀） */
    private static boolean isPlantable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        String path = Registries.ITEM.getId(stack.getItem()).getPath();
        return path.endsWith("_sapling") || path.endsWith("_seeds") || stack.isOf(Items.BAMBOO);
    }

    /** 判断是否为动物食物（小麦/胡萝卜/马铃薯/甜菜/干草块） */
    private static boolean isAnimalFood(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.isOf(Items.WHEAT) || stack.isOf(Items.CARROT)
                || stack.isOf(Items.POTATO) || stack.isOf(Items.BEETROOT)
                || stack.isOf(Items.HAY_BLOCK);
    }
}
