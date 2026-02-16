package org.emil.hnrpmc.hnessentials.listeners;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;

import java.util.UUID;

@EventBusSubscriber(modid = Hnrpmc.MODID)
public class LoggerEvents {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        log(level, Source.of(event.getPlayer()), "BREAK", event.getPos(), id(event.getState().getBlock()));
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        log(level, Source.of(event.getEntity()), "PLACE", event.getPos(), id(event.getState().getBlock()));
    }

    @SubscribeEvent
    public static void onMultiBlockPlace(BlockEvent.EntityMultiPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        for (var snapshot : event.getReplacedBlockSnapshots()) {
            BlockPos pos = snapshot.getPos();
            Block block = level.getBlockState(pos).getBlock();
            log(level, Source.of(event.getEntity()), "PLACE", pos, id(block));
        }
    }

    @SubscribeEvent
    public static void onEntityDestroyBlock(LivingDestroyBlockEvent event) {
        Level lvl = event.getEntity().level();
        if (!(lvl instanceof ServerLevel level)) return;
        log(level, Source.of(event.getEntity()), "DESTROY", event.getPos(), id(event.getState().getBlock()));
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Level lvl = event.getLevel();
        if (!(lvl instanceof ServerLevel level)) return;

        Entity directSource = event.getExplosion().getDirectSourceEntity();
        Source src = Source.of(directSource);

        for (BlockPos pos : event.getAffectedBlocks()) {
            Block block = level.getBlockState(pos).getBlock();
            if (block == net.minecraft.world.level.block.Blocks.AIR) continue;
            log(level, src, "EXPLOSION_BREAK", pos, id(block));
        }
    }

    @SubscribeEvent
    public static void onFluidReplaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        Block replaced = event.getOriginalState().getBlock();
        if (replaced == net.minecraft.world.level.block.Blocks.AIR) return;
        String transition = id(replaced) + " -> " + id(event.getNewState().getBlock());
        log(level, Source.system(), "REPLACE_BY_FLUID", pos, transition);
    }

    @SubscribeEvent
    public static void onChestOpen(PlayerInteractEvent.RightClickBlock event) {
        Level lvl = event.getLevel();
        if (!(lvl instanceof ServerLevel level)) return;

        Block block = lvl.getBlockState(event.getPos()).getBlock();
        if (block instanceof ChestBlock || block instanceof ShulkerBoxBlock) {
            log(level, Source.of(event.getEntity()), "OPEN_CONTAINER", event.getPos(), id(block));
        }
    }

    @SubscribeEvent
    public static void onItemDrop(ItemTossEvent event) {
        Level lvl = event.getPlayer().level();
        if (!(lvl instanceof ServerLevel level)) return;

        ItemStack stack = event.getEntity().getItem();
        log(level, Source.of(event.getPlayer()), "DROP_ITEM", event.getPlayer().blockPosition(), id(stack));
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        Level lvl = event.getPlayer().level();
        if (!(lvl instanceof ServerLevel level)) return;

        ItemStack stack = event.getItemEntity().getItem();
        log(level, Source.of(event.getPlayer()), "PICKUP_ITEM", event.getPlayer().blockPosition(), id(stack));
    }

    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof Player player)) return;

        Level lvl = player.level();
        if (!(lvl instanceof ServerLevel level)) return;

        log(level, Source.of(player), "KILL", event.getEntity().blockPosition(), id(event.getEntity().getType()));
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (!(player.level() instanceof ServerLevel level)) return;

        String msg = event.getMessage().getString();
        log(level, Source.of(player), "CHAT", player.blockPosition(), msg);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (HNPlayerData playerData : HNessentials.getInstance().getStorageManager().getAllPlayerData().values()) {
            ServerPlayer sp = HNessentials.getInstance().getServerPlayer(playerData.getPlayerUUID());
            if (sp == null) continue;
            boolean target = playerData.isVanish();
            if (sp.isInvisible() != target) sp.setInvisible(target);
        }
    }

    private static void log(ServerLevel level, Source source, String action, BlockPos pos, String objectName) {
        HNessentials.getInstance().getDatabaseManager().logAction(
                source.name,
                source.uuid,
                action,
                objectName,
                pos,
                level.dimension().location().toString()
        );
    }

    private static String id(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        return key == null ? "unknown:block" : key.toString();
    }

    private static String id(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String base = key == null ? "unknown:item" : key.toString();
        int count = stack.getCount();
        return count == 1 ? base : (base + " x" + count);
    }

    private static String id(EntityType<?> type) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return key == null ? "unknown:entity" : key.toString();
    }

    private static final class Source {
        final String name;
        final UUID uuid;

        private Source(String name, UUID uuid) {
            this.name = name;
            this.uuid = uuid;
        }

        static Source of(Entity entity) {
            if (entity instanceof Player p) return new Source(p.getName().getString(), p.getUUID());
            if (entity == null) return system();
            return new Source(entity.getName().getString(), entity.getUUID());
        }

        static Source system() {
            return new Source("SYSTEM", new UUID(0L, 0L));
        }
    }
}
