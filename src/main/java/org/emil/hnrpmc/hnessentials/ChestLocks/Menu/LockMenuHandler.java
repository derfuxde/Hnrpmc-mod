package org.emil.hnrpmc.hnessentials.ChestLocks.Menu;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import org.emil.hnrpmc.hnessentials.ChestLocks.config.LockData;
import org.emil.hnrpmc.hnessentials.HNessentials;

import java.util.List;
import java.util.UUID;

public class LockMenuHandler {

    // Hilfsmethode für Items mit Namen
    private static ItemStack createIcon(net.minecraft.world.level.ItemLike item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    // Erstellt einen Spielerkopf für das Menü
    private static ItemStack getPlayerHead(ServerPlayer serverPlayer, UUID uuid, String prefix) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        String name = serverPlayer.getServer().getProfileCache().get(uuid)
                .map(p -> p.getName()).orElse("Unbekannt");
        head.set(DataComponents.CUSTOM_NAME, Component.literal(prefix + name));
        head.set(DataComponents.PROFILE, new ResolvableProfile(serverPlayer.getServer().getProfileCache().get(uuid).orElseThrow()));
        return head;
    }

    // --- 1. HAUPTMENÜ ---
    public static void openMainMenu(ServerPlayer player, AttachmentHolder be) {
        if (!be.hasData(HNessentials.LOCK_DATA)) return;
        LockData data = be.getData(HNessentials.LOCK_DATA);

        player.openMenu(new SimpleMenuProvider((id, inv, p) -> {
            return new ChestMenu(MenuType.GENERIC_9x3, id, inv, new SimpleContainer(27), 3) {
                {
                    setItem(11, 0,createIcon(Items.PLAYER_HEAD, "§a§lSpieler verwalten"));
                    setItem(13, 0,createIcon(Items.PAPER, "§eBesitzer: " + player.getServer().getProfileCache().get(data.owner()).map(op -> op.getName()).orElse("?")));
                    setItem(15, 0,createIcon(Items.RED_WOOL, "§c§lSperre entfernen"));
                }

                @Override
                public void clicked(int slotId, int button, ClickType clickType, Player player) {
                    if (slotId < 0 || slotId >= 27) { super.clicked(slotId, button, clickType, player); return; }

                    if (slotId == 11) openTrustedListMenu((ServerPlayer) player, be);
                    else if (slotId == 15) {
                        be.removeData(HNessentials.LOCK_DATA);
                        player.closeContainer();
                        player.displayClientMessage(Component.literal("§aSperre entfernt!"), true);
                    }
                }
                @Override public boolean stillValid(Player p) {
                    boolean isR = false;
                    if (be instanceof BlockEntity blockEntity) {
                        isR =  !blockEntity.isRemoved();
                    } else if (be instanceof Entity entity) {
                        isR = !entity.isRemoved();
                    }
                    return isR;
                }
            };
        }, Component.literal("Schloss-Verwaltung")));
    }

    // --- 2. LISTE DER VERTRAUTEN SPIELER ---
    private static void openTrustedListMenu(ServerPlayer player, AttachmentHolder be) {
        LockData data = be.getData(HNessentials.LOCK_DATA);

        player.openMenu(new SimpleMenuProvider((id, inv, p) -> {
            return new ChestMenu(MenuType.GENERIC_9x3, id, inv, new SimpleContainer(27), 3) {
                {
                    // Liste der aktuellen Freunde anzeigen
                    int slot = 0;
                    for (UUID trustedUuid : data.trusted()) {
                        if (slot >= 18) break; // Max 18 Freunde anzeigen
                        setItem(slot++, 0,getPlayerHead(player, trustedUuid, "§cEntfernen: §f"));
                    }

                    setItem(22, 0,createIcon(Items.HOPPER, "§a§lOnline-Spieler hinzufügen"));
                    setItem(26, 0,createIcon(Items.ARROW, "§7Zurück"));
                }

                @Override
                public void clicked(int slotId, int button, ClickType clickType, Player player) {
                    if (slotId < 0 || slotId >= 27) return;

                    // Zurück
                    if (slotId == 26) openMainMenu((ServerPlayer) player, be);
                        // Add Menu
                    else if (slotId == 22) openAddPlayerMenu((ServerPlayer) player, be);
                        // Spieler entfernen
                    else if (slotId < data.trusted().size()) {
                        UUID toRemove = data.trusted().get(slotId);
                        LockData newData = new LockData(data.owner(), new java.util.ArrayList<>(data.trusted()));
                        newData.trusted().remove(toRemove);
                        be.setData(HNessentials.LOCK_DATA, newData);
                        openTrustedListMenu((ServerPlayer) player, be); // Refresh
                    }
                }
                @Override public boolean stillValid(Player p) {
                    boolean isR = false;
                    if (be instanceof BlockEntity blockEntity) {
                        isR =  !blockEntity.isRemoved();
                    } else if (be instanceof Entity entity) {
                        isR = !entity.isRemoved();
                    }
                    return isR;
                }            };
        }, Component.literal("Vertraute Spieler")));
    }

    // --- 3. ONLINE-SPIELER HINZUFÜGEN ---
    private static void openAddPlayerMenu(ServerPlayer player, AttachmentHolder be) {
        LockData data = be.getData(HNessentials.LOCK_DATA);
        List<ServerPlayer> onlinePlayers = player.getServer().getPlayerList().getPlayers();

        player.openMenu(new SimpleMenuProvider((id, inv, p) -> {
            return new ChestMenu(MenuType.GENERIC_9x6, id, inv, new SimpleContainer(54), 6) {
                {
                    int slot = 0;
                    for (ServerPlayer target : onlinePlayers) {
                        if (target.getUUID().equals(data.owner()) || data.trusted().contains(target.getUUID())) continue;
                        if (slot >= 45) break;

                        setItem(slot++, 0,getPlayerHead(player, target.getUUID(), "§aHinzufügen: §f"));
                    }
                    setItem(49, 0,createIcon(Items.ARROW, "§7Zurück"));
                }

                @Override
                public void clicked(int slotId, int button, ClickType clickType, Player player) {
                    if (slotId == 49) { openTrustedListMenu((ServerPlayer) player, be); return; }

                    ItemStack clickedStack = getSlot(slotId).getItem();
                    if (clickedStack.has(DataComponents.PROFILE)) {
                        UUID targetUuid = clickedStack.get(DataComponents.PROFILE).id().get();

                        LockData newData = new LockData(data.owner(), new java.util.ArrayList<>(data.trusted()));
                        newData.trusted().add(targetUuid);
                        be.setData(HNessentials.LOCK_DATA, newData);

                        player.displayClientMessage(Component.literal("§aSpieler hinzugefügt!"), true);
                        openTrustedListMenu((ServerPlayer) player, be); // Zurück zur Liste
                    }
                }
                @Override public boolean stillValid(Player p) {
                    boolean isR = false;
                    if (be instanceof BlockEntity blockEntity) {
                        isR =  !blockEntity.isRemoved();
                    } else if (be instanceof Entity entity) {
                        isR = !entity.isRemoved();
                    }
                    return isR;
                }
            };
        }, Component.literal("Spieler auswählen")));
    }
}