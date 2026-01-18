package org.emil.hnrpmc.simpleclans.ui.frames;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.Rank;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.emil.hnrpmc.simpleclans.ui.*;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.emil.hnrpmc.simpleclans.utils.Paginator;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class RanksFrame extends SCFrame {
	private final Paginator paginator;
	private final GameProfile toEdit;
	private final List<Rank> ranks;

	public RanksFrame(SCFrame parent, Player viewer, Clan subject, @Nullable GameProfile toEdit) {
		super(parent, (ServerPlayer) viewer);
		this.toEdit = toEdit;
		ranks = subject != null ? subject.getRanks() : new ArrayList<>();
		paginator = new Paginator(getSize() - 9, ranks);
	}

	@Override
	public void createComponents() {
		for (int slot = 0; slot < 9; slot++) {
			if (slot == 2 || slot == 4 || slot == 6 || slot == 7)
				continue;
			add(Components.getPanelComponent(slot));
		}
		
		add(Components.getBackComponent(getParent(), 2, getViewer()));

		SCComponent create = new SCComponentImpl(lang("gui.ranks.create.title", (ServerPlayer) getViewer()),
				Collections.singletonList(lang("gui.ranks.create.lore", (ServerPlayer) getViewer())), Items.WHITE_WOOL, 4);
		create.setVerifiedOnly(ClickType.PICKUP);
		create.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "rank create", false));
		create.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.leader.rank.create");
		add(create);

		add(Components.getPreviousPageComponent(6, this::previousPage, paginator, getViewer()));
		add(Components.getNextPageComponent(7, this::nextPage, paginator, getViewer()));

		int slot = 9;
		for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {
			Rank rank = ranks.get(i);
			List<String> lore;
			if (toEdit == null) {
				lore = Arrays.asList(
						lang("gui.ranks.rank.displayname.lore", (ServerPlayer) getViewer(),
								ChatUtils.parseColors(rank.getDisplayName())),
						lang("gui.ranks.rank.edit.permissions.lore", (ServerPlayer) getViewer()),
						lang("gui.ranks.rank.remove.lore", (ServerPlayer) getViewer()));
			} else {
				lore = Arrays.asList(
						lang("gui.ranks.rank.displayname.lore", (ServerPlayer) getViewer(),
								ChatUtils.parseColors(rank.getDisplayName())),
						lang("gui.ranks.rank.assign.lore", (ServerPlayer) getViewer(), toEdit.getName()));
			}
			SCComponent c = new SCComponentImpl(lang("gui.ranks.rank.title", (ServerPlayer) getViewer(), rank.getName()), lore,
					Items.FILLED_MAP, slot);
			if (toEdit != null) {
				c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(),
						"rank assign", true, toEdit.getName(), rank.getName()));
				c.setConfirmationRequired(ClickType.PICKUP);
				c.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.leader.rank.assign");
			} else {
				c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open((ServerPlayer) getViewer(), new PermissionsFrame(this, getViewer(), rank)));
				c.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.leader.rank.permissions.list");
				c.setListener(ClickType.PICKUP, ClickAction.SECONDARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(),
						"rank delete", true, rank.getName()));
				c.setConfirmationRequired(ClickType.PICKUP);
				c.setPermission(ClickType.PICKUP, ClickAction.SECONDARY, "simpleclans.leader.rank.delete");
			}
			c.setVerifiedOnly(ClickType.PICKUP);
			c.setVerifiedOnly(ClickType.PICKUP);
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
		if (toEdit != null) {
			ClanManager clanManager = SimpleClans.getInstance().getClanManager();
			String rank = clanManager.getCreateClanPlayer(toEdit.getId()).getRankId();
			return lang("gui.ranks.title.set.rank", (ServerPlayer) getViewer(), rank);
		}
		return lang("gui.ranks.title", (ServerPlayer) getViewer());
	}

	@Override
	public int getSize() {
		return 3 * 9;
	}

}
