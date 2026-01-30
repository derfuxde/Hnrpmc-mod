package org.emil.hnrpmc.hnessentials.listeners;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.emil.hnrpmc.Hnrpmc;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.managers.DatabaseManager;

@EventBusSubscriber(modid = Hnrpmc.MODID)
public class LoggerEvents {

    // 1. BLÖCKE (Abbauen & Platzieren)
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        log(event.getPlayer(), "BREAK", event.getPos(), event.getState().getBlock().toString());
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            log(player, "PLACE", event.getPos(), event.getState().getBlock().toString());
        }
    }

    // 2. CONTAINER (Truhen öffnen/schließen)
    @SubscribeEvent
    public static void onChestOpen(PlayerInteractEvent.RightClickBlock event) {
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        if (block instanceof ChestBlock || block instanceof ShulkerBoxBlock) {
            log(event.getEntity(), "OPEN_CONTAINER", event.getPos(), block.toString());
        }
    }

    // 3. ITEMS (Drop & Pickup)
    @SubscribeEvent
    public static void onItemDrop(ItemTossEvent event) {
        log(event.getPlayer(), "DROP_ITEM", event.getPlayer().blockPosition(), event.getEntity().getItem().toString());
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        log(event.getPlayer(), "PICKUP_ITEM", event.getPlayer().blockPosition(), event.getItemEntity().getItem().toString());
    }

    // 4. ENTITIES (Töten & Interagieren)
    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            log(player, "KILL", event.getEntity().blockPosition(), event.getEntity().getType().toString());
        }
    }

    // 5. CHAT (Was Spieler schreiben)
    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        log(event.getPlayer(), "CHAT", event.getPlayer().blockPosition(), event.getRawText());
    }

    // Hilfsmethode zum Kürzen
    private static void log(Player player, String action, BlockPos pos, String objectName) {
        if (player.level().isClientSide) return;

        HNessentials.getInstance().getDatabaseManager().logAction(
                player.getName().getString(),
                player.getUUID(),
                action,
                objectName, // Der Name des Blocks/Items/Entities
                pos,
                player.level().dimension().location().toString()
        );
    }
}