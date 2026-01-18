package org.emil.hnrpmc.simpleclans.ui.frames;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.ui.*;
import org.emil.hnrpmc.simpleclans.utils.Paginator;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class RivalsFrame extends SCFrame {
	private final SimpleClans plugin = SimpleClans.getInstance();
	private final Paginator paginator;
	private final Clan subject;
	private final List<String> rivals;

	public RivalsFrame(Player viewer, SCFrame parent, Clan subject) {
		super(parent, (ServerPlayer) viewer);
		this.subject = subject;
		rivals = subject.getRivals();
		paginator = new Paginator(getSize() - 9, rivals);
	}

	@Override
	public void createComponents() {

		for (int slot = 0; slot < 9; slot++) {
			if (slot == 2 || slot == 4 || slot == 6 || slot == 7)
				continue;
			add(Components.getPanelComponent(slot));
		}

		add(Components.getBackComponent(getParent(), 2, getViewer()));

		SCComponent add = new SCComponentImpl(lang("gui.rivals.add.title", (ServerPlayer) getViewer()), null, Items.RED_WOOL, 4);
		add.setVerifiedOnly(ClickType.PICKUP);
		add.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open((ServerPlayer) getViewer(), new AddRivalFrame(this, getViewer(), subject)));
		add.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, RankPermission.RIVAL_ADD);
		add(add);

		add(Components.getPreviousPageComponent(6, this::previousPage, paginator, getViewer()));
		add(Components.getNextPageComponent(7, this::nextPage, paginator, getViewer()));

		int slot = 9;
		for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {

			Clan clan = plugin.getClanManager().getClan(rivals.get(i));
			if (clan == null)
				continue;
			SCComponent c = new SCComponentImpl(
					lang("gui.clanlist.clan.title", (ServerPlayer) getViewer(), clan.getColorTag(), clan.getName()),
					Collections.singletonList(lang("gui.rivals.clan.lore", (ServerPlayer) getViewer())), Items.RED_BANNER, slot	);
			c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(),
					"rival remove", false, clan.getTag()));
			c.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, RankPermission.RIVAL_REMOVE);
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
		InventoryDrawer.open((ServerPlayer) getViewer(), this);
	}

	@Override
	public @NotNull String getTitle() {
		return lang("gui.rivals.title", (ServerPlayer) getViewer());
	}

	@Override
	public int getSize() {
		return 6 * 9;
	}
}
