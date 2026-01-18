package org.emil.hnrpmc.simpleclans.ui.frames.staff;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.emil.hnrpmc.simpleclans.ui.*;
import org.emil.hnrpmc.simpleclans.ui.frames.Components;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.GLOBAL_FRIENDLY_FIRE;
import static org.emil.hnrpmc.simpleclans.ui.frames.staff.ClanListFrame.Type;

public class StaffFrame extends SCFrame {

    public StaffFrame(@Nullable SCFrame parent, @NotNull Player viewer) {
        super(parent, (ServerPlayer) viewer);
    }

    @Override
    public @NotNull String getTitle() {
        return lang("gui.staff.title", getViewer());
    }

    @Override
    public int getSize() {
        return 3 * 9;
    }

    @Override
    public void createComponents() {
        for (int slot = 0; slot < 9; slot++) {
            if (slot == 4)
                continue;
            add(Components.getPanelComponent(slot));
        }

        add(Components.getBackComponent(getParent(), 4, getViewer()));
        addClans();
        addPlayers();
        addGlobalFf();
        addReload();
    }

    private void addClans() {
        SCComponent clanList = new SCComponentImpl.Builder(Items.PURPLE_BANNER)
                .withDisplayName(lang("gui.main.clan.list.title", getViewer())).withSlot(9)
                .withLoreLine(lang("gui.staff.clan.list.lore.left.click", getViewer()))
                .withLoreLine(lang("gui.staff.clan.list.lore.right.click", getViewer())).build();
        clanList.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), new ClanListFrame(this, getViewer(),
                Type.ALL, null)));
        clanList.setListener(ClickType.PICKUP, ClickAction.SECONDARY, () -> InventoryDrawer.open(getViewer(), new ClanListFrame(this, getViewer(),
                Type.UNVERIFIED, null)));
        add(clanList);
    }

    private void addPlayers() {
        SCComponent players = new SCComponentImpl.Builder(Items.WHITE_BANNER)
                .withDisplayName(lang("gui.staff.player.list.title", getViewer())).withSlot(10)
                .withLoreLine(lang("gui.staff.player.list.lore.left.click", getViewer()))
                .withLoreLine(lang("gui.staff.player.list.lore.right.click", getViewer())).build();
        players.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), new PlayerListFrame(getViewer(), this, false)));
        players.setListener(ClickType.PICKUP, ClickAction.SECONDARY, () -> InventoryDrawer.open(getViewer(), new PlayerListFrame(getViewer(), this, true)));
        add(players);
    }

    private void addReload() {
        SCComponent reload = new SCComponentImpl.Builder(Items.SPAWNER)
                .withDisplayName(lang("gui.staff.reload.title", getViewer())).withSlot(17)
                .withLoreLine(lang("gui.staff.reload.lore", getViewer())).build();
        reload.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.admin.reload");
        reload.setConfirmationRequired(ClickType.PICKUP);
        reload.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () ->
                InventoryController.runSubcommand(getViewer(), "admin reload", false));
        add(reload);
    }

    private void addGlobalFf() {
        SettingsManager sm = SimpleClans.getInstance().getSettingsManager();
        boolean globalffAllowed = sm.is(GLOBAL_FRIENDLY_FIRE);
        String status = globalffAllowed ? lang("allowed", getViewer()) : lang("auto", getViewer());
        SCComponent globalFf = new SCComponentImpl.Builder(Items.DIAMOND_SWORD).withSlot(12)
                .withDisplayName(lang("gui.staff.global.ff.title", getViewer()))
                .withLoreLine(lang("gui.staff.global.ff.lore.status", getViewer(), status))
                .withLoreLine(lang("gui.staff.global.ff.lore.toggle", getViewer())).build();
        globalFf.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.mod.globalff");
        globalFf.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> {
            String arg;
            if (globalffAllowed) {
                arg = "auto";
            } else {
                arg = "allow";
            }
            InventoryController.runSubcommand(getViewer(), "mod globalff", true, arg);
        });
        add(globalFf);
    }
}
