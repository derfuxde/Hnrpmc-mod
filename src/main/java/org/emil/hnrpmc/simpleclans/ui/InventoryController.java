package org.emil.hnrpmc.simpleclans.ui;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.events.ComponentClickEvent;
import org.emil.hnrpmc.simpleclans.managers.PermissionsManager;
import org.emil.hnrpmc.simpleclans.ui.frames.ConfirmationFrame;
import org.emil.hnrpmc.simpleclans.ui.frames.WarningFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.emil.hnrpmc.simpleclans.SimpleClans.getInstance;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.COMMANDS_CLAN;

@EventBusSubscriber(modid = Hnrpmc.MODID)
public class InventoryController {
    private static final Map<UUID, SCFrame> frames = new ConcurrentHashMap<>();
    private static final Set<UUID> isSwitching = Collections.newSetFromMap(new ConcurrentHashMap<>());


    // In InventoryController.java hinzufügen:

    public static void handleInternalClick(ServerPlayer player, int slotId, ClickType clickType, @Nullable ClickAction clickAction) {
        handleComponentClick(player, slotId, clickType, clickAction);
    }



    // In NeoForge nutzen wir oft das Click-Handling direkt im Container.
    // Falls du keinen eigenen Container-Typ hast, fangen wir das hier ab:
    // Hinweis: NeoForge hat kein direktes "InventoryClickEvent" wie Bukkit.
    // Man nutzt meistens eine Menu-Library oder das ServerSide-Container Handling.

    // Hier ist die Logik, wie du sie manuell verarbeiten würdest:
    public static void handleComponentClick(ServerPlayer player, int slotId, ClickType clickType, @Nullable ClickAction clickAction) {
        SCFrame frame = frames.get(player.getUUID());
        if (frame == null) return;


        SCComponent component = frame.getComponent(slotId);
        if (component == null) return;


        Runnable listener = component.getListener(clickType, clickAction);
        if (listener == null) return;

        // Clan-Verifizierung prüfen
        if (component.isVerifiedOnly(clickType) && !isClanVerified(player)) {
            InventoryDrawer.open(player, new WarningFrame(frame, player, null));
            return;
        }

        // Berechtigungen prüfen
        Object permission = component.getPermission(clickType, clickAction);
        if (permission != null) {
            if (!hasPermission(player, permission)) {
                InventoryDrawer.open(player, new WarningFrame(frame, player, permission));
                return;
            }
        }

        // Bestätigung erforderlich?
        if (component.isConfirmationRequired(clickType)) {
            listener = () -> InventoryDrawer.open(player, new ConfirmationFrame(frame, player, component.getListener(clickType, clickAction)));
        }

        Runnable finalListener = listener;
        SimpleClans.getInstance().getServer().execute(() -> {
            // Event auslösen
            ComponentClickEvent clickEvent = new ComponentClickEvent(player, frame, component);
            NeoForge.EVENT_BUS.post(clickEvent);
            if (clickEvent.isCanceled()) return;

            // "Loading..." Nachricht setzen (Lore Update)
            ItemStack item = component.getItem();
            List<Component> loadingLore = Collections.singletonList(Component.literal(lang("gui.loading", player)));
            item.set(DataComponents.LORE, new ItemLore(loadingLore));

            finalListener.run();
        });
    }

    private static boolean isClanVerified(@NotNull ServerPlayer player) {
        SimpleClans plugin = SimpleClans.getInstance();
        ClanPlayer cp = plugin.getClanManager().getAnyClanPlayer(player.getUUID());
        return cp != null && cp.getClan() != null && cp.getClan().isVerified();
    }

    private static boolean hasPermission(@NotNull ServerPlayer player, @NotNull Object permission) {
        SimpleClans plugin = SimpleClans.getInstance();
        PermissionsManager pm = plugin.getPermissionsManager();
        if (permission instanceof String perms) {
            boolean leaderPerm = perms.contains("simpleclans.leader") && !perms.equalsIgnoreCase("simpleclans.leader.create");
            ClanPlayer cp = plugin.getClanManager().getAnyClanPlayer(player.getUUID());
            return pm.has(player, perms) && (!leaderPerm || (cp != null && cp.isLeader()));
        }
        return pm.has(player, (RankPermission) permission, false);
    }

    public static void register(@NotNull SCFrame frame) {
        UUID uuid = frame.getViewer().getUUID();
        isSwitching.add(uuid); // Sperre für das Close-Event setzen
        frames.put(uuid, frame);

        // Nach 1 Tick die Sperre wieder aufheben
        SimpleClans.getInstance().getServer().execute(() -> {
            isSwitching.remove(uuid);
        });
    }

    @SubscribeEvent
    public static void onContainerClosed(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID uuid = player.getUUID();

        // WICHTIG: Erst prüfen, ob wir im "isSwitching" Modus sind
        if (isSwitching.contains(uuid)) {
            // Wir öffnen gerade ein neues Fenster, also NICHT aus der Map löschen!
            return;
        }

        // Wenn wir nicht wechseln, löschen wir den Frame
        frames.remove(uuid);
    }

    public static boolean isRegistered(@NotNull ServerPlayer player) {
        return frames.containsKey(player.getUUID());
    }

    public static void runSubcommand(@NotNull ServerPlayer player, @NotNull String subcommand, boolean update, @Nullable String... args) {
        SimpleClans plugin = SimpleClans.getInstance();
        String baseCommand = plugin.getSettingsManager().getString(COMMANDS_CLAN);
        plugin.getLogger().info("hier sind deine args {} es sind {}", Arrays.stream(args).toList(), Arrays.stream(args).toList().size());
        String finalCommand = String.format("%s %s" + (args.length == 0 || args == null ? "" : " "), baseCommand, subcommand) + String.join(" ", args);

        plugin.getServer().execute(() -> {
            // Befehl ausführen
            plugin.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), finalCommand);

            if (!update) {
                player.closeContainer();
            } else {
                SCFrame currentFrame = frames.get(player.getUUID());
                if (currentFrame instanceof ConfirmationFrame) {
                    currentFrame = currentFrame.getParent();
                }
                InventoryDrawer.open(player, currentFrame);
            }
        });
    }
}