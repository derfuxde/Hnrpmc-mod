package org.emil.hnrpmc.simpleclans.commands.clan;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.Helper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.events.HomeRegroupEvent;
import org.emil.hnrpmc.simpleclans.events.PlayerHomeClearEvent;
import org.emil.hnrpmc.simpleclans.events.PlayerHomeSetEvent;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.emil.hnrpmc.simpleclans.managers.PermissionsManager;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public final class HomeCommands extends ClanSBaseCommand {

    private final SimpleClans plugin;
    private final PermissionsManager permissions;
    private final SettingsManager settings;
    private final ClanManager cm;

    public HomeCommands(SimpleClans plugin) {
        super(plugin);
        this.plugin = plugin;
        this.permissions = plugin.getPermissionsManager();
        this.settings = plugin.getSettingsManager();
        this.cm = plugin.getClanManager();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("");
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root)
                .then(build())
                .then(buildRegroup());
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("home")
                .executes(this::homeTp)
                .then(Commands.literal("set").executes(this::homeSet))
                .then(Commands.literal("clear").executes(this::homeClear));
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildRegroup() {
        return Commands.literal("regroup")
                .then(Commands.literal("me").executes(this::regroupMe))
                .then(Commands.literal("home").executes(this::regroupHome));
    }

    private int regroupMe(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (!settings.is(ALLOW_REGROUP)) {
            ChatBlock.sendMessage(ctx.getSource(), SimpleClans.lang("insufficient.permissions", player));
            return 0;
        }
        if (!permissions.has(player, "simpleclans.leader.regroup.me")) return 0;

        Clan clan = mustClan(player, ctx.getSource());
        ClanPlayer cp = mustClanPlayer(player, ctx.getSource());
        if (!verified(player, ctx.getSource())) return 0;
        if (!rank(cp, "REGROUP_ME", ctx.getSource())) return 0;

        return processRegroup(player, cp, clan, player.position(), ctx.getSource());
    }

    private int regroupHome(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (!settings.is(ALLOW_REGROUP)) {
            ChatBlock.sendMessage(ctx.getSource(), SimpleClans.lang("insufficient.permissions", player));
            return 0;
        }
        if (!permissions.has(player, "simpleclans.leader.regroup.home")) return 0;

        Clan clan = mustClan(player, ctx.getSource());
        ClanPlayer cp = mustClanPlayer(player, ctx.getSource());
        if (!verified(player, ctx.getSource())) return 0;
        if (!rank(cp, "REGROUP_HOME", ctx.getSource())) return 0;
        if (!canTeleport(player, clan, ctx.getSource())) return 0;

        Vec3 home = clan.getHomeLocation().pos();
        if (home == null) {
            ChatBlock.sendMessage(ctx.getSource(), SimpleClans.lang("home.base.not.set", player));
            return 0;
        }

        return processRegroup(player, cp, clan, home, ctx.getSource());
    }

    private int processRegroup(ServerPlayer player, ClanPlayer cp, Clan clan, Vec3 target, CommandSourceStack src) {
        List<ServerPlayer> targets = getNonVanishedClanPlayers(player, clan);

        HomeRegroupEvent event = new HomeRegroupEvent(clan, cp, targets, player.level().dimension(), target);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 0;

        if (!cm.purchaseHomeRegroup(player)) return 0;

        //plugin.getTeleportManager().teleport(player, clan, target);
        return 1;
    }

    private int homeTp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!permissions.has(player, "simpleclans.member.home")) return 0;

        Clan clan = mustClan(player, ctx.getSource());
        ClanPlayer cp = mustClanPlayer(player, ctx.getSource());
        if (!verified(player, ctx.getSource())) return 0;
        if (!rank(cp, "HOME_TP", ctx.getSource())) return 0;
        if (!canTeleport(player, clan, ctx.getSource())) return 0;

        Vec3 home = clan.getHomeLocation().pos();
        if (home == null) {
            ChatBlock.sendMessage(ctx.getSource(), SimpleClans.lang("home.base.not.set", player));
            return 0;
        }

        if (cm.purchaseHomeTeleport(player)) {
            plugin.getTeleportManager().addPlayer(player, player.serverLevel(),home, clan.getName().toString());
            return 1;
        }
        return 0;
    }

    private int homeClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!permissions.has(player, "simpleclans.leader.home-set")) return 0;

        Clan clan = mustClan(player, ctx.getSource());
        ClanPlayer cp = mustClanPlayer(player, ctx.getSource());
        if (!verified(player, ctx.getSource())) return 0;
        if (!rank(cp, "HOME_SET", ctx.getSource())) return 0;

        if (settings.is(CLAN_HOMEBASE_CAN_BE_SET_ONLY_ONCE) && clan.getHomeLocation() != null
                && !permissions.has(player, "simpleclans.mod.home")) {
            ChatBlock.sendMessage(ctx.getSource(), SimpleClans.lang("home.base.only.once", player));
            return 0;
        }

        PlayerHomeClearEvent event = new PlayerHomeClearEvent(clan, cp);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 0;

        clan.setHomeLocation(null, null, 0.0F, 0.0F);
        ChatBlock.sendMessage(ctx.getSource(), SimpleClans.lang("hombase.cleared", player));
        return 1;
    }

    private int homeSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!permissions.has(player, "simpleclans.leader.home-set")) return 0;

        Clan clan = mustClan(player, ctx.getSource());
        ClanPlayer cp = mustClanPlayer(player, ctx.getSource());
        if (!verified(player, ctx.getSource())) return 0;
        if (!rank(cp, "HOME_SET", ctx.getSource())) return 0;

        if (settings.is(CLAN_HOMEBASE_CAN_BE_SET_ONLY_ONCE) && clan.getHomeLocation() != null
                && !permissions.has(player, "simpleclans.mod.home")) {
            ChatBlock.sendMessage(ctx.getSource(), SimpleClans.lang("home.base.only.once", player));
            return 0;
        }

        Vec3 pos = player.position();

        PlayerHomeSetEvent event = new PlayerHomeSetEvent(clan, cp, pos);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 0;

        if (!cm.purchaseHomeTeleportSet(player)) return 0;

        clan.setHomeLocation(player.serverLevel(), pos, 0.0F, 0.0F);
        ChatBlock.sendMessage(ctx.getSource(),
                SimpleClans.lang("hombase.set", player, Helper.toLocationString(pos, player.serverLevel())));
        return 1;
    }

    // ---------------- helpers ----------------

    private Clan mustClan(ServerPlayer player, CommandSourceStack src) {
        Clan c = cm.getClanByPlayerUniqueId(player.getUUID());
        if (c == null) {
            ChatBlock.sendMessage(src, String.valueOf(Component.literal("You are not in a clan.")));
            throw new IllegalStateException("Player not in clan");
        }
        return c;
    }

    private ClanPlayer mustClanPlayer(ServerPlayer player, CommandSourceStack src) {
        ClanPlayer cp = cm.getClanPlayer(player.getUUID());
        if (cp == null) {
            ChatBlock.sendMessage(src, String.valueOf(Component.literal("ClanPlayer missing.")));
            throw new IllegalStateException("ClanPlayer missing");
        }
        return cp;
    }

    private boolean verified(ServerPlayer player, CommandSourceStack src) {
        return org.emil.hnrpmc.simpleclans.commands.conditions.Conditions.verified(player);
    }

    private boolean rank(ClanPlayer cp, String rankName, CommandSourceStack src) {
        return org.emil.hnrpmc.simpleclans.commands.conditions.Conditions.rank(cp, rankName);
    }

    private boolean canTeleport(ServerPlayer player, Clan clan, CommandSourceStack src) {
        return false;
    }

    private List<ServerPlayer> getNonVanishedClanPlayers(ServerPlayer requester, Clan clan) {
        return plugin.getVanishService().getNonVanished(requester, clan);
    }
}
