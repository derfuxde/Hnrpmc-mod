package org.emil.hnrpmc.simpleclans.ui.frames;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.ui.InventoryDrawer;
import org.emil.hnrpmc.simpleclans.ui.SCComponent;
import org.emil.hnrpmc.simpleclans.ui.SCComponentImpl;
import org.emil.hnrpmc.simpleclans.ui.SCFrame;
import org.emil.hnrpmc.simpleclans.ui.frames.staff.PlayerDetailsFrame;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.emil.hnrpmc.simpleclans.utils.Paginator;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class RosterFrame extends SCFrame {

	private final Clan subject;
	private final boolean staff;
	private final List<ClanPlayer> allMembers;
	private final Paginator paginator;

	public RosterFrame(Player viewer, SCFrame parent, Clan subject) {
		this(viewer, parent, subject, false);
	}

	public RosterFrame(Player viewer, SCFrame parent, Clan subject, boolean staff) {
		super(parent, (ServerPlayer) viewer);
		this.subject = subject;
		this.staff = staff;

		allMembers = subject.getLeaders();
		allMembers.addAll(subject.getNonLeaders());
		paginator = new Paginator(getSize() - 9, allMembers.size());
	}

	@Override
	public void createComponents() {
		for (int slot = 0; slot < 9; slot++) {
			if (slot == 2 || slot == 4 || slot == 6 || slot == 7)
				continue;
			add(Components.getPanelComponent(slot));
		}

		add(Components.getBackComponent(getParent(), 2, getViewer()));

		if (!staff) {
			SCComponent invite = new SCComponentImpl(lang("gui.roster.invite.title", (ServerPlayer) getViewer()),
					Collections.singletonList(lang("gui.roster.invite.lore", (ServerPlayer) getViewer())), Items.LIME_WOOL, 4);
			invite.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open((ServerPlayer) getViewer(),new InviteFrame(this, getViewer())));
			invite.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, RankPermission.INVITE);
			add(invite);
		} else {
			add(Components.getPanelComponent(4));
		}

		add(Components.getPreviousPageComponent(6, this::previousPage, paginator, getViewer()));
		add(Components.getNextPageComponent(7, this::nextPage, paginator, getViewer()));

		int slot = 9;
		for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {
			ClanPlayer cp = allMembers.get(i);
			SCComponent playerComponent = Components.getPlayerComponent(this, getViewer(), cp, slot,
					true);
			if (staff) {
				playerComponent.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open((ServerPlayer) getViewer(),
						new PlayerDetailsFrame((ServerPlayer) getViewer(), this, SimpleClans.getInstance().getServer().getProfileCache().get(cp.getUniqueId()).get())));
			}
			add(playerComponent);
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
		InventoryDrawer.open((ServerPlayer) this.getViewer(), this);
	}

	@Override
	public @NotNull String getTitle() {
		return lang("gui.roster.title", (ServerPlayer) getViewer(), ChatUtils.stripColors(subject.getColorTag()));
	}

	@Override
	public int getSize() {
		return 6 * 9;
	}

}
