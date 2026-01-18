package org.emil.hnrpmc.simpleclans.ui.frames;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.Helper;
import org.emil.hnrpmc.simpleclans.Rank;
import org.emil.hnrpmc.simpleclans.ui.*;
import org.emil.hnrpmc.simpleclans.utils.Paginator;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class AddPermissionFrame extends SCFrame {
	private final String[] availablePermissions;
	private final Paginator paginator;
	private final Rank rank;

	public AddPermissionFrame(SCFrame parent, Player viewer, Rank rank) {
		super(parent, (ServerPlayer) viewer);
		this.rank = rank;
		Set<String> rankPerms = rank.getPermissions();
		availablePermissions = Arrays.stream(Helper.fromPermissionArray()).filter(p -> !rankPerms.contains(p))
				.toArray(String[]::new);
		paginator = new Paginator(getSize() - 9, availablePermissions.length);
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

			String permission = availablePermissions[i];

			SCComponent c = new SCComponentImpl(
					lang("gui.add.permission.permission.title",getViewer(), permission),
					Collections.singletonList(lang("gui.add.permission.permission.lore",getViewer())),
					Items.PAPER, slot);
			c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand(getViewer(),
					"rank permissions add", true, rank.getName(), permission));
			c.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.leader.rank.permissions.add");
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
		return lang("gui.add.permission.title",getViewer());
	}

	@Override
	public int getSize() {
		return 3 * 9;
	}

}
