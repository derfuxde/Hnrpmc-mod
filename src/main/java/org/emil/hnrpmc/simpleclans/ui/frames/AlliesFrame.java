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

public class AlliesFrame extends SCFrame {
	private final SimpleClans plugin = SimpleClans.getInstance();
	private final Paginator paginator;
	private final List<String> allies;
	private final Clan subject;

	public AlliesFrame(Player viewer, SCFrame parent, Clan subject) {
		super(parent, (ServerPlayer) viewer);
		this.subject = subject;
		allies = subject.getAllies();
		paginator = new Paginator(getSize() - 9, allies.size());
	}

	@Override
	public void createComponents() {
		for (int slot = 0; slot < 9; slot++) {
			if (slot == 2 || slot == 4 || slot == 6 || slot == 7)
				continue;
			add(Components.getPanelComponent(slot));
		}

		add(Components.getBackComponent(getParent(), 2, getViewer()));

		SCComponent add = new SCComponentImpl(lang("gui.allies.add.title",getViewer()), null,
				Items.CYAN_WOOL, 4);
		add.setVerifiedOnly(ClickType.PICKUP);
		add.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), new AddAllyFrame(this, getViewer(), subject)));
		add.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, RankPermission.ALLY_ADD);
		add(add);

		add(Components.getPreviousPageComponent(6, this::previousPage, paginator, getViewer()));
		add(Components.getNextPageComponent(7, this::nextPage, paginator, getViewer()));

		int slot = 9;
		for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {

			Clan clan = plugin.getClanManager().getClan(allies.get(i));
			if (clan == null)
				continue;
			SCComponent c = new SCComponentImpl(
					lang("gui.clanlist.clan.title",getViewer(), clan.getColorTag(), clan.getName()),
					Collections.singletonList(lang("gui.allies.clan.lore",getViewer())), Items.CYAN_BANNER, slot);
			c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand(getViewer(),
					"ally remove", false, clan.getTag()));
			c.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, RankPermission.ALLY_REMOVE);
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
		return lang("gui.allies.title",getViewer());
	}

	@Override
	public int getSize() {
		return 6 * 9;
	}

}
