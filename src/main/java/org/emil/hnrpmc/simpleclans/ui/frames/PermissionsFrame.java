package org.emil.hnrpmc.simpleclans.ui.frames;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.Rank;
import org.emil.hnrpmc.simpleclans.ui.*;
import org.emil.hnrpmc.simpleclans.utils.Paginator;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class PermissionsFrame extends SCFrame {
	private final Rank rank;
	private final Paginator paginator;
	private final String[] permissions;

	public PermissionsFrame(SCFrame parent, Player viewer, Rank rank) {
		super(parent, (ServerPlayer) viewer);
		this.rank = rank;
		permissions = rank.getPermissions().toArray(new String[0]);
		paginator = new Paginator(getSize() - 9, permissions.length);
	}

	@Override
	public void createComponents() {
		for (int slot = 0; slot < 9; slot++) {
			if (slot == 2 || slot == 4 || slot == 6 || slot == 7)
				continue;
			add(Components.getPanelComponent(slot));
		}

		add(Components.getBackComponent(getParent(), 2, getViewer()));

		SCComponent add = new SCComponentImpl(lang("gui.permissions.add.title",getViewer()), null, Items.WHITE_WOOL,
				4);
		add.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), new AddPermissionFrame(this, getViewer(), rank)));
		add.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.leader.rank.permissions.add");
		add(add);

		add(Components.getPreviousPageComponent(6, this::previousPage, paginator, getViewer()));
		add(Components.getNextPageComponent(7, this::nextPage, paginator, getViewer()));

		int slot = 9;
		for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {

			String permission = permissions[i];

			SCComponent c = new SCComponentImpl(lang("gui.permissions.permission.title",getViewer(), permission),
					Collections.singletonList(lang("gui.permissions.permission.lore",getViewer())), Items.PAPER, slot);
			c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand(getViewer(),
					"rank permissions remove", true, rank.getName(), permission));
			c.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.leader.rank.permissions.remove");
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
		return lang("gui.permissions.title",getViewer(), rank.getName());
	}

	@Override
	public int getSize() {
		return 3 * 9;
	}

}
