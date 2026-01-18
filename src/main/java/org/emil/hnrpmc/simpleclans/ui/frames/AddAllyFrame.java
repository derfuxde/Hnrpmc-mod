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
import java.util.stream.Collectors;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class AddAllyFrame extends SCFrame {
	private final List<Clan> notAllies;
	private final Paginator paginator;

	public AddAllyFrame(SCFrame parent, Player viewer, Clan subject) {
		super(parent, (ServerPlayer) viewer);
		SimpleClans plugin = SimpleClans.getInstance();
		notAllies = plugin.getClanManager().getClans().stream()
				.filter(c -> !c.equals(subject) && !c.isRival(subject.getTag()) && !c.isAlly(subject.getTag()))
				.collect(Collectors.toList());
		paginator = new Paginator(getSize() - 9, notAllies);
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

			Clan notRival = notAllies.get(i);
			SCComponent c = new SCComponentImpl(
					lang("gui.clanlist.clan.title", getViewer(), notRival.getColorTag(), notRival.getName()),
					Collections.singletonList(lang("gui.add.ally.clan.lore", getViewer())), Items.CYAN_BANNER,
					slot);

			c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand(getViewer(),
					"ally add", false, notRival.getTag()));
			c.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, RankPermission.ALLY_ADD);
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
		return lang("gui.add.ally.title",getViewer());
	}

	@Override
	public int getSize() {
		return 6 * 9;
	}
}
