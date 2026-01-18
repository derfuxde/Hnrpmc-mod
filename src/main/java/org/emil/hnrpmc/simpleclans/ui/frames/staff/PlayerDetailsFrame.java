package org.emil.hnrpmc.simpleclans.ui.frames.staff;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.ui.*;
import org.emil.hnrpmc.simpleclans.ui.frames.Components;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.ui.frames.staff.ClanListFrame.Type;

public class PlayerDetailsFrame extends SCFrame {

	private final GameProfile subject;
	private final String subjectName;

	public PlayerDetailsFrame(@NotNull Player viewer, SCFrame parent, @NotNull GameProfile subject) {
		super(parent, (ServerPlayer) viewer);
		this.subject = subject;
		subjectName = SimpleClans.getInstance().getClanManager().getCreateClanPlayer(subject.getId()).getName();
	}

	@Override
	public void createComponents() {
		for (int slot = 0; slot < 9; slot++) {
			if (slot == 4)
				continue;
			add(Components.getPanelComponent(slot));
		}

		add(Components.getBackComponent(getParent(), 4, getViewer()));
		add(Components.getPlayerComponent((SCFrame) this, getViewer(), subject, 13, false));

		addBanUnban();
		addPlace();
		addResetKDR();
		addPurge();
		addPromoteDemote();
	}

	private void addPromoteDemote() {
		SCComponent promoteDemote = new SCComponentImpl(lang("gui.playerdetails.promote.demote.title",getViewer()),
				Arrays.asList(lang("gui.playerdetails.promote.lore.left.click", getViewer()),
						lang("gui.playerdetails.demote.lore.right.click", getViewer())),
				Items.GUNPOWDER, 28);
		promoteDemote.setConfirmationRequired(ClickType.PICKUP);
		promoteDemote.setListener(ClickType.PICKUP, ClickAction.PRIMARY,
				() -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "admin promote", true, subjectName));
		promoteDemote.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.admin.promote");
		promoteDemote.setListener(ClickType.PICKUP, ClickAction.SECONDARY,
				() -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "admin demote", true, subjectName));
		promoteDemote.setConfirmationRequired(ClickType.PICKUP);
		add(promoteDemote);
		promoteDemote.setPermission(ClickType.PICKUP, ClickAction.SECONDARY, "simpleclans.admin.demote");
	}

	private void addPurge() {
		SCComponent purge = new SCComponentImpl.Builder(Items.LAVA_BUCKET).withSlot(34).withDisplayName(
				lang("gui.playerdetails.purge.title", getViewer())).withLoreLine(
				lang("gui.playerdetails.purge.lore", getViewer())).build();
		purge.setConfirmationRequired(ClickType.PICKUP);
		purge.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(),
				"admin purge", false, subjectName));
		purge.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.admin.purge");
		add(purge);
	}

	private void addResetKDR() {
		SCComponent resetKdr = new SCComponentImpl.Builder(Items.ANVIL)
				.withSlot(30).withDisplayName(lang("gui.main.reset.kdr.title", getViewer()))
				.withLoreLine(lang("gui.playerdetails.resetkdr.lore", getViewer())).build();
		resetKdr.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(),
				"admin resetkdr", false, subjectName));
		resetKdr.setConfirmationRequired(ClickType.PICKUP);
		resetKdr.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.admin.resetkdr");
		add(resetKdr);
	}

	private void addPlace() {
		SCComponent place = new SCComponentImpl.Builder(Items.MINECART).withSlot(32)
				.withDisplayName(lang("gui.playerdetails.place.title", getViewer()))
				.withLoreLine(lang("gui.playerdetails.place.lore", getViewer())).build();
		place.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.mod.place");
		place.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), new ClanListFrame((SCFrame) this, (Player) getViewer(),
				Type.PLACE, subject)));
		add(place);
	}

	private void addBanUnban() {
		SCComponent banUnban = new SCComponentImpl.Builder(Items.BARRIER).withSlot(40)
				.withDisplayName(lang("gui.playerdetails.ban.unban.title", getViewer()))
				.withLore(Arrays.asList(lang("gui.playerdetails.ban.left.click", getViewer()),
						lang("gui.playerdetails.unban.right.click", getViewer()))).build();
		banUnban.setConfirmationRequired(ClickType.PICKUP);
		banUnban.setConfirmationRequired(ClickType.PICKUP);
		banUnban.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.mod.ban");
		banUnban.setPermission(ClickType.PICKUP, ClickAction.SECONDARY, "simpleclans.mod.ban");
		banUnban.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(),
				"mod ban", false, subjectName));
		banUnban.setListener(ClickType.PICKUP, ClickAction.SECONDARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(),
				"mod unban", false, subjectName));
		add(banUnban);
	}

	@Override
	public @NotNull String getTitle() {
		return lang("gui.playerdetails.title", getViewer(), subjectName);
	}

	@Override
	public int getSize() {
		return 6 * 9;
	}

}
