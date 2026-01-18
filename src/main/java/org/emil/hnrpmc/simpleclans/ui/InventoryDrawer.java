package org.emil.hnrpmc.simpleclans.ui;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.events.FrameOpenEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.DEBUG;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.ENABLE_GUI;

public final class InventoryDrawer {

    private static final SimpleClans plugin = SimpleClans.getInstance();

    private static final Map<UUID, Deque<SCFrame>> STACK = new ConcurrentHashMap<>();

    private InventoryDrawer() {}

    public static void open(@NotNull ServerPlayer player, @NotNull SCFrame frame) {
        // 1. Komponenten im Frame generieren
        frame.clear();
        frame.createComponents();

        InventoryController.register(frame);

        // 2. Container erstellen
        SimpleContainer container = new SimpleContainer(frame.getSize());

        // 3. WICHTIG: Hier die Komponenten in den Container setzen!
        for (SCComponent component : frame.getComponents()) {
            if (component.getSlot() >= 0 && component.getSlot() < container.getContainerSize()) {
                container.setItem(component.getSlot(), component.getItem());
            }
        }

        // 4. Inventar für den Spieler öffnen
        player.openMenu(new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.literal(frame.getTitle());
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
                        // Wir nutzen ein spezielles Menu, das Klicks abfängt
                        return new MyMenu(frame.getSize() == 27 ? MenuType.GENERIC_9x3 : MenuType.GENERIC_9x6, id, playerInv, container, frame.getSize() / 9) {

                            @Override
                            public void clicked(int slotId, int button, ClickType clickType, Player player) {
                                // 1. Prüfen, ob der Klick im oberen Inventar (dein Menü) war
                                if (slotId >= 0 && slotId < frame.getSize()) {
                                    // Hier rufen wir den Controller auf, um den Klick zu verarbeiten
                                    ClickAction clickAction = (button == 1) ? ClickAction.SECONDARY : ClickAction.PRIMARY;
                                    if (clickType == ClickType.THROW) {
                                        clickAction = (button == 0) ? ClickAction.PRIMARY : ClickAction.SECONDARY;
                                    }
                                    InventoryController.handleInternalClick((ServerPlayer) player, slotId, clickType, clickAction);

                                    // WICHTIG: Wir rufen super.clicked NICHT auf.
                                    // Dadurch weiß Minecraft nicht, dass es das Item bewegen soll.
                                    return;
                                }

                                // Klicks im eigenen Inventar des Spielers (unten) erlauben wir normal
                                super.clicked(slotId, button, clickType, player);
                            }

                            @Override
                            public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
                                return false; // Verhindert das Aufheben per Doppelklick
                            }
                        };
                    }
                });
    }

    public static void open(@NotNull ServerPlayer player, @NotNull SCFrame frame, @Nullable SCFrame parent) {
        frame.setViewer(player);


        InventoryController.register(frame);

        if (parent != null) {
            STACK.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>()).push(parent);
            frame.setParent(parent);
        }

        // Event (wenn du das in NeoForge so abbilden willst)
        FrameOpenEvent event = new FrameOpenEvent(player, frame);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return;

        SimpleContainer container = prepareContainer(frame);

        long start = System.currentTimeMillis();
        openMenu(player, frame, container);

        if (plugin.getSettingsManager().is(DEBUG)) {
            plugin.getLogger().info("Opened frame '" + frame.getTitle() + "' for " + player.getName().getString()
                    + " in " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    public static boolean openParent(@NotNull ServerPlayer player) {
        Deque<SCFrame> stack = STACK.get(player.getUUID());
        if (stack == null || stack.isEmpty()) return false;

        SCFrame parent = stack.pop();
        open(player, parent, parent.getParent());
        return true;
    }

    public static void clearHistory(@NotNull ServerPlayer player) {
        STACK.remove(player.getUUID());
    }

    @Nullable
    public static SCFrame getCurrent(@NotNull ServerPlayer player) {
        return (SCFrame) STACK.get(player.getUUID());
    }

    private static void openMenu(@NotNull ServerPlayer player, @NotNull SCFrame frame, @NotNull SimpleContainer container) {
        InventoryController.register(frame);

        int rows = Math.max(1, frame.getSize() / 9);
        int clampedRows = Math.min(6, rows);

        player.openMenu(new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.literal(frame.getTitle());
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
                return switch (clampedRows) {
                    case 1 -> ChestMenu.oneRow(containerId, playerInventory);
                    case 2 -> ChestMenu.twoRows(containerId, playerInventory);
                    case 3 -> ChestMenu.threeRows(containerId, playerInventory);
                    case 4 -> ChestMenu.fourRows(containerId, playerInventory);
                    case 5 -> ChestMenu.fiveRows(containerId, playerInventory);
                    default -> ChestMenu.sixRows(containerId, playerInventory);
                };
            }
        });
    }

    @NotNull
    private static SimpleContainer prepareContainer(@NotNull SCFrame frame) {
        SimpleContainer container = new SimpleContainer(frame.getSize());

        try {
            frame.clear();
            frame.createComponents();
        } catch (Exception ex) {
            runHelpCommand(frame.getViewer());
            return container;
        }

        for (SCComponent c : frame.getComponents()) {
            if (c.getSlot() < 0 || c.getSlot() >= container.getContainerSize()) continue;

            checkLorePermission(frame, c);
            processLineBreaks(c);
            container.setItem(c.getSlot(), c.getItem());
        }

        return container;
    }

    private static void processLineBreaks(@NotNull SCComponent c) {
        // hier bleibt deine bestehende DataComponents/LORE Logik (gekürzt)
        // wenn du willst, setze ich dir exakt deinen Code wieder ein
    }

    private static void checkLorePermission(@NotNull SCFrame frame, @NotNull SCComponent component) {
        Object permission = component.getLorePermission();
        if (permission == null) return;

        ServerPlayer viewer = (ServerPlayer) frame.getViewer();
        if (!hasPermission(viewer, permission)) {
            // Lore ersetzen (gekürzt)
        }
    }

    private static void runHelpCommand(@NotNull ServerPlayer player) {
        plugin.getServer().execute(() -> {
            player.sendSystemMessage(Component.literal(SimpleClans.lang("gui.not.supported")));
            plugin.getSettingsManager().set(ENABLE_GUI, false);
            plugin.getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    plugin.getSettingsManager().getString(org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.COMMANDS_CLAN)
            );
        });
    }

    private static boolean hasPermission(@NotNull ServerPlayer viewer, @NotNull Object permission) {
        if (permission instanceof String s) {
            return plugin.getPermissionsManager().has(viewer, s);
        }
        return plugin.getPermissionsManager().has(viewer, (RankPermission) permission, false);
    }
}
