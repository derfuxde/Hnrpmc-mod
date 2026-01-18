package org.emil.hnrpmc.simpleclans.ui.frames;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.emil.hnrpmc.simpleclans.ui.*;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.CLAN_CONFIRMATION_FOR_DEMOTE;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.CLAN_CONFIRMATION_FOR_PROMOTE;

public class PlayerDetailsFrame extends SCFrame {

	private final SimpleClans plugin = SimpleClans.getInstance();
	private final GameProfile subject;
	private final String subjectName;
	private final Clan clan;

	public PlayerDetailsFrame(Player viewer, SCFrame parent, GameProfile subject) {
		super(parent, (ServerPlayer) viewer);
		this.subject = subject;
		ClanPlayer cp = plugin.getClanManager().getCreateClanPlayer(subject.getId());
		subjectName = cp.getName();
		clan = cp.getClan();
	}

	@Override
	public void createComponents() {
		for (int slot = 0; slot < 9; slot++) {
			if (slot == 4)
				continue;
			add(Components.getPanelComponent(slot));
		}

		add(Components.getBackComponent(getParent(), 4, getViewer()));
		add(Components.getPlayerComponent(this, getViewer(), subject, 13, false));

		if (!isSameClan()) {
			return;
		}

		addKick();
		addPromoteDemote();
		addAssignUnassign();
		addTrustUntrust();
	}

	private void addKick() {
		SCComponent kick = new SCComponentImpl(lang("gui.playerdetails.kick.title",getViewer()), null, Items.RED_WOOL,
				28);
		kick.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "kick", true, subjectName));
		kick.setConfirmationRequired(ClickType.PICKUP);
		kick.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, RankPermission.KICK);
		add(kick);
	}

	private void addPromoteDemote() {
		SettingsManager settings = plugin.getSettingsManager();
		SCComponent promoteDemote = new SCComponentImpl(lang("gui.playerdetails.promote.demote.title",getViewer()),
				Arrays.asList(lang("gui.playerdetails.promote.lore.left.click",getViewer()),
						lang("gui.playerdetails.demote.lore.right.click",getViewer())),
				Items.GUNPOWDER, 30);
		promoteDemote.setConfirmationRequired(ClickType.PICKUP);
		promoteDemote.setListener(ClickType.PICKUP, ClickAction.PRIMARY,
				() -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "promote", !settings.is(CLAN_CONFIRMATION_FOR_PROMOTE), subjectName));
		promoteDemote.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.leader.promote");
		promoteDemote.setListener(ClickType.PICKUP, ClickAction.SECONDARY,
				() -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "demote", !settings.is(CLAN_CONFIRMATION_FOR_DEMOTE), subjectName));
		promoteDemote.setConfirmationRequired(ClickType.PICKUP);
		add(promoteDemote);
		promoteDemote.setPermission(ClickType.PICKUP, ClickAction.SECONDARY, "simpleclans.leader.demote");
	}

	private void addAssignUnassign() {
		SCComponentImpl assignUnassign = new SCComponentImpl(lang("gui.playerdetails.assign.unassign.title",getViewer()),
				Arrays.asList(lang("gui.playerdetails.assign.lore.left.click",getViewer()),
						lang("gui.playerdetails.unassign.lore.right.click",getViewer())),
				Items.FEATHER, 32);
		assignUnassign.setConfirmationRequired(ClickType.PICKUP);
		assignUnassign.setListener(ClickType.PICKUP, ClickAction.PRIMARY,
				() -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "rank unassign", true, subjectName));
		assignUnassign.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.leader.rank.unassign");
		assignUnassign.setListener(ClickType.PICKUP, ClickAction.SECONDARY,
				() -> InventoryDrawer.open(getViewer(), new RanksFrame(this, getViewer(), clan, subject)));
		add(assignUnassign);
		assignUnassign.setPermission(ClickType.PICKUP, ClickAction.SECONDARY, "simpleclans.leader.rank.assign");
	}

	private void addTrustUntrust() {
		SCComponent trustUntrust = new SCComponentImpl(lang("gui.playerdetails.trust.untrust.title",getViewer()),
				Arrays.asList(lang("gui.playerdetails.trust.lore.left.click",getViewer()),
						lang("gui.playerdetails.untrust.lore.right.click",getViewer())),
				Items.CYAN_DYE, 34);
		trustUntrust.setConfirmationRequired(ClickType.PICKUP);
		trustUntrust.setListener(ClickType.PICKUP, ClickAction.PRIMARY,
				() -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "trust", true, subjectName));
		trustUntrust.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.leader.settrust");
		trustUntrust.setListener(ClickType.PICKUP, ClickAction.SECONDARY,
				() -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "untrust", true, subjectName));
		trustUntrust.setPermission(ClickType.PICKUP, ClickAction.SECONDARY, "simpleclans.leader.settrust");
		trustUntrust.setConfirmationRequired(ClickType.PICKUP);
		add(trustUntrust);
	}

	@Override
	public @NotNull String getTitle() {
		return lang("gui.playerdetails.title",getViewer(), subjectName);
	}

	@Override
	public int getSize() {
		int size = 3;
		if (isSameClan()) {
			size = 6;
		}
		return size * 9;
	}

	private boolean isSameClan() {
		return clan != null && clan.isMember(subject.getId());
	}

}
