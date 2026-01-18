package org.emil.hnrpmc.simpleclans.ui.frames.staff;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.ui.InventoryController;
import org.emil.hnrpmc.simpleclans.ui.InventoryDrawer;
import org.emil.hnrpmc.simpleclans.ui.SCComponent;
import org.emil.hnrpmc.simpleclans.ui.SCFrame;
import org.emil.hnrpmc.simpleclans.ui.frames.Components;
import org.emil.hnrpmc.simpleclans.utils.Paginator;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class ClanListFrame extends SCFrame {
	private final Type type;
	private final @Nullable GameProfile toPlace;
	private List<Clan> clans;
	private final Paginator paginator;

	public ClanListFrame(@Nullable SCFrame parent, @NotNull Player viewer, @NotNull Type type, @Nullable GameProfile toPlace) {
		super(parent, (ServerPlayer) viewer);
		this.type = type;
		this.toPlace = toPlace;
		SimpleClans plugin = SimpleClans.getInstance();
		clans = plugin.getClanManager().getClans();
		if (type == Type.UNVERIFIED) {
			clans = clans.stream().filter(c -> !c.isVerified()).collect(Collectors.toList());
		}
		paginator = new Paginator(getSize() - 9, clans);
		plugin.getClanManager().sortClansByName(clans, true);
	}

	@Override
	public void createComponents() {
		for (int slot = 0; slot < 9; slot++) {
			if (slot == 2 || slot == 6 || slot == 7)
				continue;
			add(Components.getPanelComponent(slot));
		}
		add(Components.getBackComponent(getParent(), 2, getViewer()));

		add(Components.getPreviousPageComponent(6, this::previousPage, paginator, getViewer()));
		add(Components.getNextPageComponent(7, this::nextPage, paginator, getViewer()));

		int slot = 9;
		for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {
			Clan clan = clans.get(i);
			SCComponent c = Components.getClanComponent(this, getViewer(), clan, slot, false);
			if (type != Type.PLACE) {
				c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () ->
						InventoryDrawer.open(getViewer(), new ClanDetailsFrame(this, getViewer(), clan)));
			} else {
				if (toPlace != null) {
					c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand(getViewer(),
							"mod place", false, toPlace.getName(), clan.getTag()));
					c.setConfirmationRequired(ClickType.PICKUP);
					c.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.mod.place");
				}
			}
			add(c);
			slot++;
		}
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
		if (type == Type.PLACE && toPlace != null) {
			return lang("gui.staff.clanlist.toplace.title", getViewer(), toPlace.getName());
		}
		if (type == Type.UNVERIFIED) {
			return lang("gui.staff.clanlist.unverified.title", getViewer(), clans.size());
		}
		return lang("gui.clanlist.title", getViewer(), clans.size());
	}

	@Override
	public int getSize() {
		return 6 * 9;
	}

	public enum Type {
		ALL, UNVERIFIED, PLACE
	}
}
