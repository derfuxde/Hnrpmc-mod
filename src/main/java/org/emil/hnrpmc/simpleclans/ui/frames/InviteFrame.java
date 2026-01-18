package org.emil.hnrpmc.simpleclans.ui.frames;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.emil.hnrpmc.simpleclans.ui.*;
import org.emil.hnrpmc.simpleclans.utils.Paginator;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.ECONOMY_INVITE_PRICE;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.ECONOMY_PURCHASE_CLAN_INVITE;

public class InviteFrame extends SCFrame {

	private final Paginator paginator;
	private final SimpleClans plugin;
	private final List<Player> players;

	public InviteFrame(SCFrame parent, Player viewer) {
		super(parent, (ServerPlayer) viewer);
		this.plugin = SimpleClans.getInstance();
		ClanManager cm = plugin.getClanManager();
		players = plugin.getServer().getPlayerList().getPlayers().stream().filter(p -> cm.getClanPlayer(p) == null)
				.collect(Collectors.toList());
		paginator = new Paginator(getSize() - 9, players.size());
	}

	@Override
	public void createComponents() {
		addHeader();

		int slot = 9;
		for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {

			Player player = players.get(i);
			SCComponent c = createPlayerComponent(player, slot);
			add(c);
			slot++;
		}
	}

	@NotNull
	private SCComponent createPlayerComponent(@NotNull Player player, int slot) {
		double price = plugin.getSettingsManager().is(ECONOMY_PURCHASE_CLAN_INVITE) ? plugin.getSettingsManager().getDouble(ECONOMY_INVITE_PRICE) : 0;
		List<String> lore = new ArrayList<>();
		lore.add(lang("gui.invite.player.lore", getViewer()));

		SCComponent c = new SCComponentImpl(
				lang("gui.invite.player.title", getViewer(), player.getName()), lore, Items.PLAYER_HEAD, slot);
		GameProfile offlinePlayer = SimpleClans.getInstance().getServer().getProfileCache().get(player.getUUID()).get();
		Components.setOwningPlayer(c.getItem(), offlinePlayer);
		c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand(getViewer(), "invite", false, String.valueOf(player.getName())));
		c.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, RankPermission.INVITE);
		return c;
	}

	public void addHeader() {
		for (int slot = 0; slot < 9; slot++) {
			if (slot == 2 || slot == 6 || slot == 7)
				continue;
			add(Components.getPanelComponent(slot));
		}
		add(Components.getBackComponent(getParent(), 2, getViewer()));

		add(Components.getPreviousPageComponent(6, this::previousPage, paginator, getViewer()));
		add(Components.getNextPageComponent(7, this::nextPage, paginator, getViewer()));
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
		return lang("gui.invite.title",getViewer());
	}
	
	@Override
	public int getSize() {
		return 3 * 9;
	}
}
