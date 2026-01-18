package org.emil.hnrpmc.simpleclans.ui.frames.staff;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.ui.*;
import org.emil.hnrpmc.simpleclans.ui.frames.Components;
import org.emil.hnrpmc.simpleclans.ui.frames.RosterFrame;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class ClanDetailsFrame extends SCFrame {
    private final Clan clan;

    public ClanDetailsFrame(@Nullable SCFrame parent, @NotNull Player viewer, @NotNull Clan clan) {
        super(parent, (ServerPlayer) viewer);
        this.clan = clan;
    }

    @Override
    public void createComponents() {
        for (int slot = 0; slot < 9; slot++) {
            if (slot == 4)
                continue;
            add(Components.getPanelComponent(slot));
        }

        add(Components.getBackComponent(getParent(), 4, getViewer()));
        add(Components.getClanComponent(this, getViewer(), clan, 13, false));

        addRoster();
        addHome();
        addVerify();
        addDisband();
    }

    private void addDisband() {
        SCComponent disband = new SCComponentImpl(lang("gui.clandetails.disband.title", getViewer()),
                Collections.singletonList(lang("gui.staffclandetails.disband.lore", getViewer())),
                Items.BARRIER, 34);
        disband.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand(getViewer(),
                "mod disband", false, clan.getTag()));
        disband.setConfirmationRequired(ClickType.PICKUP);
        disband.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.mod.disband");
        add(disband);
    }

    private void addVerify() {
        boolean verified = clan.isVerified();

        Item material = verified ? Items.REDSTONE_TORCH : Items.LEVER;
        String title = verified ? lang("gui.clandetails.verified.title", getViewer())
                : lang("gui.clandetails.not.verified.title", getViewer());
        List<String> lore = verified ? null : new ArrayList<>();
        if (!verified) {
            lore.add(lang("gui.staffclandetails.not.verified.lore", getViewer()));
        }
        SCComponent verify = new SCComponentImpl(title, lore, material, 32);
        if (!verified) {
            verify.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.mod.verify");
            verify.setConfirmationRequired(ClickType.PICKUP);
            verify.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand(getViewer(),
                    "mod verify", false, clan.getTag()));
        }
        add(verify);
    }

    private void addHome() {
        List<String> lore = new ArrayList<>();
        lore.add(lang("gui.staffclandetails.home.lore.teleport", getViewer()));
        lore.add(lang("gui.staffclandetails.home.lore.set", getViewer()));

        SCComponent home = new SCComponentImpl(lang("gui.clandetails.home.title", getViewer()), lore,
                Objects.requireNonNull(Items.MAGENTA_BED.getDefaultInstance()), 30);
        home.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand(getViewer(),
                "mod home tp", false, clan.getTag()));
        home.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.mod.hometp");
        home.setListener(ClickType.PICKUP, ClickAction.SECONDARY, () -> InventoryController.runSubcommand(getViewer(),
                "mod home set", false, clan.getTag()));
        home.setPermission(ClickType.PICKUP, ClickAction.SECONDARY, "simpleclans.mod.home");
        home.setConfirmationRequired(ClickType.PICKUP);
        add(home);
    }

    private void addRoster() {
        SCComponent roster = new SCComponentImpl(lang("gui.clandetails.roster.title", getViewer()),
                Collections.singletonList(lang("gui.staffclandetails.roster.lore", getViewer())),
                Items.PLAYER_HEAD, 28);

        List<ClanPlayer> members = clan.getMembers();
        if (members.size() != 0) {
            GameProfile offlinePlayer = SimpleClans.getInstance().getServer().getProfileCache().get(
                    members.get((int) (Math.random() * members.size())).getUniqueId()).get();
            Components.setOwningPlayer(roster.getItem(), offlinePlayer);
        }

        roster.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), new RosterFrame(getViewer(), this, clan, true)));
        add(roster);
    }

    @Override
    public @NotNull String getTitle() {
        return lang("gui.clandetails.title", getViewer(), ChatUtils.stripColors(clan.getColorTag()),
                clan.getName());
    }

    @Override
    public int getSize() {
        return 6 * 9;
    }

}
