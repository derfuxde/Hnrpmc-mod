package org.emil.hnrpmc.simpleclans.ui.frames;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.PermissionLevel;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.ui.SCComponent;
import org.emil.hnrpmc.simpleclans.ui.SCComponentImpl;
import org.emil.hnrpmc.simpleclans.ui.SCFrame;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class WarningFrame extends SCFrame {
	private final SimpleClans plugin = SimpleClans.getInstance();
	private final Object permission;

	public WarningFrame(@NotNull SCFrame parent, @NotNull Player viewer, @Nullable Object permission) {
		super(parent, (ServerPlayer) viewer);
		this.permission = permission;
	}

	@Override
	public void createComponents() {
		for (int slot = 0; slot < 9; slot++) {
			if (slot == 4)
				continue;
			add(Components.getPanelComponent(slot));
		}
		add(Components.getBackComponent(getParent(), 4, getViewer()));

		int slot = 22;
		if (permission != null) {
			addNoPermissionComponent(permission, slot);
		} else {
			addNotVerifiedComponent(slot);
		}
	}

	private void addNotVerifiedComponent(int slot) {
		SCComponent verified = new SCComponentImpl(lang("gui.warning.not.verified.title", (ServerPlayer) getViewer()),
				Collections.singletonList(lang("gui.warning.not.verified.lore", (ServerPlayer) getViewer())), Items.LEVER, slot);
		add(verified);
	}

	private void addNoPermissionComponent(Object permission, int slot) {
		List<String> lore;
		if (permission instanceof String) {
			lore = Collections.singletonList(lang("gui.warning.no.permission.plugin.lore", (ServerPlayer) getViewer()));
			ClanPlayer cp = plugin.getClanManager().getAnyClanPlayer(getViewer().getUUID());
			if (((String) permission).contains("simpleclans.leader") && !cp.isLeader()) {
				lore = Collections.singletonList(lang("gui.warning.no.permission.leader.lore", (ServerPlayer) getViewer()));
			}
		} else {
			RankPermission p = (RankPermission) permission;
			String level = p.getPermissionLevel() == PermissionLevel.LEADER ? lang("leader", (ServerPlayer) getViewer())
					: lang("trusted", (ServerPlayer) getViewer());
			lore = Collections.singletonList(lang("gui.warning.no.permission.rank.lore", (ServerPlayer) getViewer(), level, p.toString()));
		}
		SCComponent perm = new SCComponentImpl(lang("gui.warning.no.permission.title", (ServerPlayer) getViewer()), lore,
                Items.BARRIER, slot);
		add(perm);
	}

	@Override
	public @NotNull String getTitle() {
		return lang("gui.warning.title", (ServerPlayer) getViewer());
	}

	@Override
	public int getSize() {
		return 6 * 9;
	}

}
