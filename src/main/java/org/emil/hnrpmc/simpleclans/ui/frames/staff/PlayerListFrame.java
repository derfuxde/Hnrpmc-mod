package org.emil.hnrpmc.simpleclans.ui.frames.staff;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.ui.InventoryDrawer;
import org.emil.hnrpmc.simpleclans.ui.SCComponent;
import org.emil.hnrpmc.simpleclans.ui.SCFrame;
import org.emil.hnrpmc.simpleclans.ui.frames.Components;
import org.emil.hnrpmc.simpleclans.utils.Paginator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class PlayerListFrame extends SCFrame {

    private final boolean onlineOnly;
    private Paginator paginator;
    private List<GameProfile> players;

    public PlayerListFrame(@NotNull ServerPlayer viewer, @Nullable SCFrame parent, boolean onlineOnly) {
        super(parent, viewer);
        this.onlineOnly = onlineOnly;
        loadPlayers();
    }

    @Override
    public void createComponents() {
        // Grundpanel-Komponenten (Slots 0-8 oben)
        for (int slot = 0; slot < 9; slot++) {
            if (slot == 2 || slot == 6 || slot == 7) continue;
            add(Components.getPanelComponent(slot));
        }

        // Navigation
        add(Components.getBackComponent(getParent(), 2, getViewer()));
        add(Components.getPreviousPageComponent(6, this::previousPage, paginator, getViewer()));
        add(Components.getNextPageComponent(7, this::nextPage, paginator, getViewer()));

        // Spieler-Icons (Ab Slot 9)
        int slot = 9;
        for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {
            GameProfile playerProfile = players.get(i);

            // Nutzt die universelle Component-Fabrik für Spieler-Köpfe
            SCComponent c = Components.getPlayerComponent(this, getViewer(), playerProfile, slot, false);

            // Click-Event: Öffnet die Details für den ausgewählten Spieler
            c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), new PlayerDetailsFrame(getViewer(), this, playerProfile)));

            add(c);
            slot++;
        }
    }

    private void loadPlayers() {
        MinecraftServer server = getViewer().getServer();
        if (server == null) return;

        if (onlineOnly) {
            // Nur aktuell verbundene Spieler
            players = server.getPlayerList().getPlayers().stream()
                    .map(ServerPlayer::getGameProfile)
                    .collect(Collectors.toList());
        } else {
            // Alle bekannten Spieler (Clan-Mitglieder + Online)
            players = SimpleClans.getInstance().getClanManager().getAllClanPlayers().stream()
                    .map(cp -> {
                        // Versuche Profil aus Cache zu holen, sonst erstelle temporäres Profil
                        return server.getProfileCache()
                                .get(cp.getUniqueId())
                                .orElse(new GameProfile(cp.getUniqueId(), cp.getName()));
                    })
                    .distinct()
                    .collect(Collectors.toList());
        }

        // Filtern und Sortieren
        players = players.stream()
                .filter(p -> p.getName() != null && !p.getName().isEmpty())
                .sorted(Comparator.comparing(GameProfile::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        // Paginator initialisieren (Größe des Inventars minus Kopfzeile)
        paginator = new Paginator(getSize() - 9, players.size());
    }

    private void previousPage() {
        if (paginator.previousPage()) {
            updateFrame();
        }
    }

    private void nextPage() {
        if (paginator.nextPage()) {
            updateFrame();
        }
    }

    private void updateFrame() {
        InventoryDrawer.open(getViewer(), this);
    }

    @Override
    public @NotNull String getTitle() {
        return lang("gui.player.list.title", getViewer());
    }

    @Override
    public int getSize() {
        return 6 * 9; // 54 Slots (Standard Chest-Größe)
    }
}