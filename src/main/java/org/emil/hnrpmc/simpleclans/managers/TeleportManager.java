package org.emil.hnrpmc.simpleclans.managers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.TeleportState;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "hnrpmc")
public final class TeleportManager {
    private final SimpleClans plugin;
    // UUID als Key ist in Forge performanter
    private final Map<UUID, TeleportState> waitingPlayers = new ConcurrentHashMap<>();
    private int tickCounter = 0;

    public TeleportManager(SimpleClans plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(ServerPlayer player, ServerLevel destinationLevel, Vec3 destination, String clanName) {
        // Permissions-Check (LuckPerms kompatibel über hasPermissions oder PermissionAPI)
        int secs = 5; // Hier deinen SettingsManager nutzen: plugin.getSettingsManager().getInt(...)

        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUUID());

        if (user != null) {
            if (player.hasPermissions(2) || user.getCachedData().getPermissionData().checkPermission("simpleclans.mod.bypass").asBoolean()) {
                secs = 0;
            }
        }

        waitingPlayers.put(player.getUUID(), new TeleportState(player, destinationLevel, destination, clanName, secs));

        if (secs > 0) {
            player.sendSystemMessage(Component.literal("§bTeleport in " + secs + " Sekunden. Bitte nicht bewegen!"));
        }
    }

    // Das Herzstück: Ersetzt den startCounter() Bukkit-Scheduler
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // 1. Holen der Hauptinstanz
        SimpleClans plugin = SimpleClans.getInstance();
        if (plugin == null) {
            return;
        }

        // 2. Holen des Managers und expliziter Null-Check
        TeleportManager tpManager = plugin.getTeleportManager();
        if (tpManager == null) {
            // Der Manager ist noch nicht initialisiert, wir überspringen diesen Tick
            return;
        }

        // 3. Logik ausführen
        tpManager.tickCounter++;

        // Nur alle 20 Ticks (1 Sekunde) ausführen
        if (tpManager.tickCounter % 20 == 0) {
            tpManager.processQueue(event.getServer());
        }

        if (tpManager.tickCounter % 1200 == 0) {

            if (ChatManager.isDiscordHookEnabled(plugin)) {
                plugin.getChatManager().getDiscordHook(plugin).updatename();
            }
        }

    }

    private void processQueue(net.minecraft.server.MinecraftServer server) {
        Iterator<Map.Entry<UUID, TeleportState>> iter = waitingPlayers.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<UUID, TeleportState> entry = iter.next();
            TeleportState state = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player == null) {
                iter.remove();
                continue;
            }

            // Bewegung prüfen (isSameBlock)
            if (!isSameBlock(player.position(), state.getOriginPos())) {
                player.sendSystemMessage(Component.literal("§cDu hast dich bewegt! Teleport abgebrochen."));
                iter.remove();
                continue;
            }

            if (state.isTeleportTime()) {
                executeTeleport(player, state);
                iter.remove();
            } else {
                player.sendSystemMessage(Component.literal("§b" + state.getCounter()));
            }
        }
    }

    private void executeTeleport(ServerPlayer player, TeleportState state) {
        // Hier könntest du dein Custom Event feuern (NeoForge EventBus)

        dropItems(player);

        // Eigentlicher Teleport
        player.teleportTo(
                state.getDestinationLevel(),
                state.getDestinationPos().x,
                state.getDestinationPos().y,
                state.getDestinationPos().z,
                player.getYRot(),
                player.getXRot()
        );

        player.sendSystemMessage(Component.literal("§bDu wurdest zur Clan-Base teleportiert!"));
    }

    private boolean isSameBlock(Vec3 loc1, Vec3 loc2) {
        return (int)loc1.x == (int)loc2.x && (int)loc1.y == (int)loc2.y && (int)loc1.z == (int)loc2.z;
    }

    private void dropItems(ServerPlayer player) {
        // Hier deine Item-Drop Logik (Items aus SettingsManager prüfen)
        // Beispiel für einen Drop in NeoForge:
        /*
        ItemStack stack = player.getInventory().getItem(0);
        ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
        player.level().addFreshEntity(entity);
        */
    }

    // Hilfsmethode für "Sicheren Teleport" (erhöht Y bis Luft gefunden wird)
    public Vec3 getSafePos(ServerLevel level, Vec3 pos) {
        BlockPos blockPos = BlockPos.containing(pos);
        int y = blockPos.getY();

        while (y < level.getMaxBuildHeight()) {
            if (level.getBlockState(blockPos.atY(y)).isAir() && level.getBlockState(blockPos.atY(y + 1)).isAir()) {
                return new Vec3(pos.x, y, pos.z);
            }
            y++;
        }
        return pos;
    }
}