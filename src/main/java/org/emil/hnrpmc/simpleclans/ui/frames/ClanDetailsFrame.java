package org.emil.hnrpmc.simpleclans.ui.frames;

import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.ClanPlayer.Channel;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.emil.hnrpmc.simpleclans.ui.*;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.emil.hnrpmc.simpleclans.utils.VanishUtils;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public class ClanDetailsFrame extends SCFrame {
	private final Clan clan;
	private final ClanPlayer cp;
	private final SimpleClans plugin;
	private final SettingsManager settings;

	public ClanDetailsFrame(@Nullable SCFrame parent, @NotNull Player viewer, @NotNull Clan clan) {
		super(parent, (ServerPlayer) viewer);
		this.clan = clan;
		plugin = SimpleClans.getInstance();
		settings = plugin.getSettingsManager();
		cp = plugin.getClanManager().getClanPlayer(getViewer());
	}

	@Override
	public void createComponents() {
		for (int slot = 0; slot < 9; slot++) {
			if (slot == 4)
				continue;
			add(Components.getPanelComponent(slot));
		}

		add(Components.getBackComponent(getParent(), 4, getViewer()));
		add(Components.getClanComponent(this, getViewer(), clan, 13, false));

		addRoster();
		addCoords();
		addAllies();
		addRivals();
		addHome();
		addRegroup();
		addFf();
		addFee();
		addRank();
		addVerify();
		addResign();
		addDisband();
		addChat();
	}

	private void addChat() {
		Channel cpChannel = cp.getChannel();
		boolean clanEnabled = Channel.CLAN.equals(cpChannel);
		boolean allyEnabled = Channel.ALLY.equals(cpChannel);

		SCComponent chat = createChatComponent(clanEnabled, allyEnabled);
		chat.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> {
			if (clanEnabled) {
				cp.setChannel(Channel.NONE);
			} else {
				cp.setChannel(Channel.CLAN);
			}
			updateFrame();
		});
		chat.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.member.chat");
		chat.setListener(ClickType.PICKUP, ClickAction.SECONDARY, () -> {
			if (allyEnabled) {
				cp.setChannel(Channel.NONE);
			} else {
				cp.setChannel(Channel.ALLY);
			}
			updateFrame();
		});
		chat.setPermission(ClickType.PICKUP, ClickAction.SECONDARY, RankPermission.ALLY_CHAT);
		add(chat);
	}

	@NotNull
	private SCComponent createChatComponent(boolean clanEnabled, boolean allyEnabled) {
		String joined = lang("chat.joined", getViewer());
		String notJoined = lang("chat.not.joined", getViewer());

		String clanStatus = clanEnabled ? joined : notJoined;
		String allyStatus = allyEnabled ? joined : notJoined;

		String chatCommand = settings.is(CLANCHAT_TAG_BASED) ? clan.getTag() : settings.getString(COMMANDS_CLAN_CHAT);
		String joinArg = lang("join", getViewer());
		String leaveArg = lang("leave", getViewer());
		return new SCComponentImpl(lang("gui.clandetails.chat.title", getViewer()),
				Arrays.asList(
						lang("gui.clandetails.chat.clan.chat.lore", getViewer(), chatCommand),
						lang("gui.clandetails.chat.clan.join.leave.lore", getViewer(), chatCommand, joinArg, leaveArg),
						lang("gui.clandetails.chat.ally.chat.lore", getViewer(), settings.getString(COMMANDS_ALLY)),
						lang("gui.clandetails.chat.ally.join.leave.lore", getViewer(), settings.getString(COMMANDS_ALLY), joinArg, leaveArg),
						lang("gui.clandetails.chat.clan.status.lore", getViewer(), clanStatus),
						lang("gui.clandetails.chat.ally.status.lore", getViewer(), allyStatus),
						lang("gui.clandetails.chat.clan.toggle.lore", getViewer()),
						lang("gui.clandetails.chat.ally.toggle.lore", getViewer())),
				Items.KNOWLEDGE_BOOK, 43);
	}

	private void addRank() {
		SCComponent rank = new SCComponentImpl(lang("gui.clandetails.rank.title", getViewer()),
				Collections.singletonList(lang("gui.clandetails.rank.lore", getViewer())), Items.IRON_HELMET,
				37);
		rank.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), new RanksFrame(this, getViewer(), clan, null)));
		rank.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.leader.rank.list");
		add(rank);
	}

	private void addFee() {
		String status = clan.isMemberFeeEnabled() ? lang("fee.enabled", (ServerPlayer) getViewer()) : lang("fee.disabled", getViewer());
		SCComponent fee = new SCComponentImpl(lang("gui.clandetails.fee.title", getViewer()),
				Arrays.asList(lang("gui.clandetails.fee.value.lore", getViewer(), clan.getMemberFee()),
						lang("gui.clandetails.fee.status.lore", getViewer(), status),
						lang("gui.clandetails.fee.toggle.lore", getViewer())),
				Items.GOLD_NUGGET, 41);
		fee.setVerifiedOnly(ClickType.PICKUP);
		fee.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "toggle fee", true));
		fee.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, RankPermission.FEE_ENABLE);
		add(fee);
	}

	private void addDisband() {
		SCComponent disband = new SCComponentImpl(lang("gui.clandetails.disband.title", getViewer()),
				Collections.singletonList(lang("gui.clandetails.disband.lore", getViewer())), Items.BARRIER,
				50);
		disband.setListener(ClickType.THROW, ClickAction.PRIMARY, () -> InventoryController.runSubcommand(getViewer(), "disband", false));
		disband.setPermission(ClickType.THROW, ClickAction.PRIMARY, "simpleclans.leader.disband");
		disband.setConfirmationRequired(ClickType.PICKUP);
		add(disband);
	}

	private void addResign() {
		SCComponent resign = new SCComponentImpl(lang("gui.clandetails.resign.title", getViewer()),
				Collections.singletonList(lang("gui.clandetails.resign.lore", getViewer())), Items.IRON_DOOR, 48);
		resign.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "resign", false));
		resign.setConfirmationRequired(ClickType.PICKUP);
		resign.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.member.resign");
		add(resign);
	}

	private void addVerify() {
		boolean verified = clan.isVerified();
		boolean purchaseVerification = settings.is(REQUIRE_VERIFICATION) && settings.is(ECONOMY_PURCHASE_CLAN_VERIFY);

		Item material = verified ? Items.REDSTONE_TORCH : Items.LEVER;
		String title = verified ? lang("gui.clandetails.verified.title", getViewer())
				: lang("gui.clandetails.not.verified.title", getViewer());
		List<String> lore = verified ? null : new ArrayList<>();
		if (!verified) {
			lore.add(lang("gui.clandetails.not.verified.lore", getViewer()));
		}
		SCComponent verify = new SCComponentImpl(title, lore, material, 39);
		if (!verified) {
			verify.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.leader.verify");
			verify.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "verify", false));
		}
		add(verify);
	}

	private void addFf() {
		String personalFf = cp.isFriendlyFire() ? lang("allowed",getViewer()) : lang("auto",getViewer());
		String clanFf = clan.isFriendlyFire() ? lang("allowed", getViewer()) : lang("blocked", getViewer());
		SCComponent ff = new SCComponentImpl(lang("gui.clandetails.ff.title", getViewer()),
				Arrays.asList(lang("gui.clandetails.ff.personal.lore", getViewer(), personalFf),
						lang("gui.clandetails.ff.clan.lore", getViewer(), clanFf),
						lang("gui.clandetails.ff.personal.toggle.lore", getViewer()),
						lang("gui.clandetails.ff.clan.toggle.lore", getViewer())),
				Items.GOLDEN_SWORD, 32);

		ff.setListener(ClickType.PICKUP, ClickAction.PRIMARY, this::togglePersonalFf);
		ff.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.member.ff");
		ff.setListener(ClickType.PICKUP, ClickAction.SECONDARY, this::toggleClanFf);
		ff.setPermission(ClickType.PICKUP, ClickAction.SECONDARY, RankPermission.FRIENDLYFIRE);
		add(ff);
	}

	private void toggleClanFf() {
		String arg;
		if (clan.isFriendlyFire()) {
			arg = "block";
		} else {
			arg = "allow";
		}
		InventoryController.runSubcommand((ServerPlayer) getViewer(), "clanff", true, arg);
	}

	private void togglePersonalFf() {
		String arg;
		if (cp.isFriendlyFire()) {
			arg = "auto";
		} else {
			arg = "allow";
		}
		InventoryController.runSubcommand(getViewer(), "ff", true, arg);
	}

	private void addRegroup() {
		double price = 0;
		if (settings.is(ECONOMY_PURCHASE_HOME_REGROUP)) {
			price = settings.getDouble(ECONOMY_REGROUP_PRICE);
			if (!settings.is(ECONOMY_UNIQUE_TAX_ON_REGROUP)) {
				price = price * VanishUtils.getNonVanished(getViewer(), clan).size();
			}
		}

		List<String> lore = new ArrayList<>();
		lore.add(lang("gui.clandetails.regroup.lore.home", getViewer()));
		lore.add(lang("gui.clandetails.regroup.lore.me", getViewer()));

		SCComponent regroup = new SCComponentImpl(lang("gui.clandetails.regroup.title", getViewer()), lore,
				Items.BEACON, 30);
		regroup.setVerifiedOnly(ClickType.PICKUP);
		regroup.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "regroup home", false));
		regroup.setConfirmationRequired(ClickType.PICKUP);
		regroup.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, RankPermission.REGROUP_HOME);
		regroup.setVerifiedOnly(ClickType.PICKUP);
		regroup.setListener(ClickType.PICKUP, ClickAction.SECONDARY, () -> InventoryController.runSubcommand(getViewer(), "regroup me", false));
		regroup.setConfirmationRequired(ClickType.PICKUP);
		regroup.setPermission(ClickType.PICKUP, ClickAction.SECONDARY, RankPermission.REGROUP_ME);
		add(regroup);
	}

	private void addHome() {
		double homePrice = settings.is(ECONOMY_PURCHASE_HOME_TELEPORT) ? settings.getDouble(ECONOMY_HOME_TELEPORT_PRICE) : 0;
		double setPrice = settings.is(ECONOMY_PURCHASE_HOME_TELEPORT_SET) ? settings.getDouble(ECONOMY_HOME_TELEPORT_SET_PRICE) : 0;

		List<String> lore = new ArrayList<>();
		lore.add(lang("gui.clandetails.home.lore.teleport", getViewer()));
		lore.add(lang("gui.clandetails.home.lore.set", getViewer()));
		lore.add(lang("gui.clandetails.home.lore.clear", getViewer()));

		SCComponent home = new SCComponentImpl(lang("gui.clandetails.home.title", getViewer()), lore,
				Objects.requireNonNull(Items.MAGENTA_BED), 28);
		home.setVerifiedOnly(ClickType.PICKUP);
		home.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryController.runSubcommand((ServerPlayer) getViewer(), "home", false));
		home.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, RankPermission.HOME_TP);
		home.setVerifiedOnly(ClickType.PICKUP);
		home.setListener(ClickType.PICKUP, ClickAction.SECONDARY, () -> InventoryController.runSubcommand(getViewer(), "home set", false));
		home.setPermission(ClickType.PICKUP, ClickAction.SECONDARY, RankPermission.HOME_SET);
		home.setConfirmationRequired(ClickType.PICKUP);
		home.setVerifiedOnly(ClickType.THROW);
		home.setListener(ClickType.THROW, ClickAction.PRIMARY, () -> InventoryController.runSubcommand(getViewer(), "home clear", false));
		home.setPermission(ClickType.THROW, ClickAction.PRIMARY, RankPermission.HOME_SET);
		home.setConfirmationRequired(ClickType.THROW);
		add(home);
	}

	private void addRoster() {
		SCComponent roster = new SCComponentImpl(lang("gui.clandetails.roster.title", getViewer()),
				Collections.singletonList(lang("gui.clandetails.roster.lore", getViewer())), Items.PLAYER_HEAD,
				19);
		List<ClanPlayer> members = clan.getMembers();
		if (members.size() != 0) {
            ClanPlayer randomCP = members.get((int) (Math.random() * members.size()));

            GameProfile offlinePlayer = SimpleClans.getInstance().getServer().getProfileCache()
                    .get(randomCP.getUniqueId())
                    .orElse(new GameProfile(randomCP.getUniqueId(), randomCP.getName()));
			Components.setOwningPlayer(roster.getItem(), offlinePlayer);
		}
		roster.setVerifiedOnly(ClickType.PICKUP);
		roster.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), new RosterFrame(getViewer(), this, clan)));
		roster.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.member.roster");
		add(roster);
	}

	private void addCoords() {
		SCComponent coords = new SCComponentImpl(lang("gui.clandetails.coords.title", getViewer()),
				Collections.singletonList(lang("gui.clandetails.coords.lore", getViewer())), Items.COMPASS,
				21);
		coords.setVerifiedOnly(ClickType.PICKUP);
		coords.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), new CoordsFrame(getViewer(), this, clan)));
		coords.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, RankPermission.COORDS);
		add(coords);
	}

	private void addAllies() {
		SCComponent allies = new SCComponentImpl(lang("gui.clandetails.allies.title", getViewer()),
				Collections.singletonList(lang("gui.clandetails.allies.lore", getViewer())), Items.CYAN_BANNER,
				23);
		allies.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), new AlliesFrame(getViewer(), this, clan)));
		allies.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.anyone.alliances");
		add(allies);
	}

	private void addRivals() {
		SCComponent rivals = new SCComponentImpl(lang("gui.clandetails.rivals.title", getViewer()),
				Collections.singletonList(lang("gui.clandetails.rivals.lore", getViewer())), Items.RED_BANNER,
				25);
		rivals.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), new RivalsFrame(getViewer(), this, clan)));
		rivals.setPermission(ClickType.PICKUP, ClickAction.PRIMARY, "simpleclans.anyone.rivalries");
		add(rivals);
	}

	private void updateFrame() {
		InventoryDrawer.open(getViewer(), this);
	}

	@Override
	public @NotNull String getTitle() {
		return lang("gui.clandetails.title",getViewer(), ChatUtils.stripColors(clan.getColorTag()),
				clan.getName());
	}

	@Override
	public int getSize() {
		return 6 * 9;
	}

}
