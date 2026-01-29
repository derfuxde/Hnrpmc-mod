package org.emil.hnrpmc.simpleclans.commands.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.hnessentials.ChestLocks.config.LockConfig;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.SCCommandManager;
import org.emil.hnrpmc.simpleclans.commands.clan.Suggestions;
import org.emil.hnrpmc.simpleclans.config.ClanConfig;
import org.emil.hnrpmc.simpleclans.config.NameRulesStore;
import org.emil.hnrpmc.simpleclans.events.*;
import org.emil.hnrpmc.simpleclans.language.LanguageResource;
import org.emil.hnrpmc.simpleclans.managers.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.GLOBAL_FRIENDLY_FIRE;

public final class StaffCommands extends ClanSBaseCommand {

    private SimpleClans plugin;
    private final ClanManager cm;
    private final PermissionsManager permissions;
    private final SettingsManager settings;
    private final StorageManager storage;

    public StaffCommands(SimpleClans plugin) {
        super(plugin);
        this.plugin = plugin;
        this.cm = plugin.getClanManager();
        this.permissions = plugin.getPermissionsManager();
        this.settings = plugin.getSettingsManager();
        this.storage = plugin.getStorageManager();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("");
    }

    @Override
    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    /**
     * /clan mod ...
     * /clan admin ...
     */
    public LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root)
                .requires(src -> src.getEntity() instanceof ServerPlayer)
                .then(mod())
                .then(admin());
    }

    // ------------------------------------------------------------
    // MOD COMMANDS
    // ------------------------------------------------------------

    private LiteralArgumentBuilder<CommandSourceStack> mod() {
        return Commands.literal("mod")
                .requires(src -> has(src, "simpleclans.mod"))
                .then(place())
                .then(modtag())
                .then(home())
                .then(ban())
                .then(unban())
                .then(globalff())
                .then(verify())
                .then(kick())
                .then(disband())
                .then(rename())
                .then(locale());
    }

    // /clan mod place <player> <clan>
    private LiteralArgumentBuilder<CommandSourceStack> place() {
        return Commands.literal("place")
                .requires(src -> has(src, "simpleclans.mod.place"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(Suggestions.allPlayers(plugin, false))
                        .then(Commands.argument("clan", StringArgumentType.word())
                                .suggests(Suggestions.Allclans(plugin))
                                .executes(this::execPlace)));
    }

    private LiteralArgumentBuilder<CommandSourceStack> reloadconfig() {
        return Commands.literal("reloadconfig")
                .requires(src -> has(src, "simpleclans.mod"))
                    .executes(this::execconfig);
    }

    private int execconfig(CommandContext<CommandSourceStack> ctx) {
        plugin.getSettingsManager().loadAndSave();
        return 1;
    }

    private int execPlace(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String playerName = StringArgumentType.getString(ctx, "player");
        String clanTagOrName = StringArgumentType.getString(ctx, "clan");

        UUID uuid = cm.getClanPlayer(playerName).getUniqueId(); // -> du brauchst so eine Methode oder ersetzt das
        Clan newClan = resolveClan(clanTagOrName);
        if (newClan == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Clan not found.");
            return 0;
        }

        ClanPlayer oldCp = cm.getClanPlayer(uuid);

        if (oldCp.getClan() != null) {
            Clan oldClan = Objects.requireNonNull(oldCp.getClan());

            if (oldClan.equals(newClan)) {
                ChatBlock.sendMessage(src, lang("player.already.in.this.clan", (ServerPlayer) src.getEntity()));
                return 0;
            }

            if (!oldClan.isPermanent() && oldClan.isLeader(uuid) && oldClan.getLeaders().size() <= 1) {
                ChatBlock.sendMessage(src, ChatFormatting.RED + lang("you.cannot.move.the.last.leader", (ServerPlayer) src.getEntity()));
                return 0;
            }

            oldClan.addBb(oldCp.getName(), lang("0.has.resigned", (ServerPlayer) null, oldCp.getName()));
            oldClan.removePlayerFromClan(uuid);
        }

        ClanPlayer cp = oldCp;
        if (cp == null) {
            cp = cm.getCreateClanPlayer(uuid);
        }
        newClan.addBb(lang("joined.the.clan", (ServerPlayer) null, oldCp.getName()));
        cm.serverAnnounce(lang("has.joined", (ServerPlayer) null, oldCp.getName(), newClan.getName()));
        newClan.addPlayerToClan(oldCp);
        return 1;
    }

    // /clan mod modtag <clan> <tag>
    private LiteralArgumentBuilder<CommandSourceStack> modtag() {
        return Commands.literal("modtag")
                .requires(src -> has(src, "simpleclans.mod.modtag"))
                .then(Commands.argument("clan", StringArgumentType.word())
                        .suggests(Suggestions.Allclans(plugin))
                        .then(Commands.argument("tag", StringArgumentType.word())
                                .executes(this::execModtag)));
    }

    private int execModtag(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        String clanArg = StringArgumentType.getString(ctx, "clan");
        String tag = StringArgumentType.getString(ctx, "tag");

        Clan clan = resolveClan(clanArg);
        if (clan == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Clan not found.");
            return 0;
        }

        TagChangeEvent event = new TagChangeEvent(player, clan, tag);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 0;

        tag = event.getNewTag();
        String cleanTag = Helper.cleanTag(tag);

        Optional<String> validationError = plugin.getTagValidator().validate(player, tag);
        if (validationError.isPresent()) {
            ChatBlock.sendMessage(src, validationError.get());
            return 0;
        }

        if (!cleanTag.equals(clan.getTag())) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + lang("you.can.only.modify.the.color.and.case.of.the.tag", player));
            return 0;
        }

        clan.addBb(player.getName().getString(), lang("tag.changed.to.0", player, tag));
        clan.changeClanTag(tag);
        ChatBlock.sendMessage(src, lang("0.tag.changed.to.1", player, clan.getTag(), tag));
        return 1;
    }

    // /clan mod home set <clan>
    // /clan mod home tp <clan>
    private LiteralArgumentBuilder<CommandSourceStack> home() {
        return Commands.literal("home")
                .then(Commands.literal("set")
                        .requires(src -> has(src, "simpleclans.mod.home"))
                        .then(Commands.argument("clan", StringArgumentType.word())
                                .suggests(Suggestions.Allclans(plugin))
                                .executes(this::execHomeSet)))
                .then(Commands.literal("tp")
                        .requires(src -> has(src, "simpleclans.mod.hometp"))
                        .then(Commands.argument("clan", StringArgumentType.word())
                                .suggests(Suggestions.Allclans(plugin))
                                .executes(this::execHomeTp)));
    }

    private int execHomeSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ClanPlayer cp = cm.getClanPlayer(player.getUUID());
        String clanArg = StringArgumentType.getString(ctx, "clan");
        Clan clan = resolveClan(clanArg);
        if (clan == null) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Clan not found.");
            return 0;
        }

        var loc = player.position(); // du nutzt bei dir vermutlich eigene Location-Wrapper; hier nur Platzhalter
        PlayerHomeSetEvent event = new PlayerHomeSetEvent(clan, cp, player.position());
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 0;

        //clan.setHome(player.level().dimension(), player.blockPosition(), player.getYRot(), player.getXRot());
        ChatBlock.sendMessage(ctx.getSource(),
                ChatFormatting.AQUA + lang("hombase.mod.set", player, clan.getName()) + " " +
                        ChatFormatting.YELLOW + Helper.toLocationString(player.position(), player.serverLevel()));
        return 1;
    }

    private int execHomeTp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String clanArg = StringArgumentType.getString(ctx, "clan");
        Clan clan = resolveClan(clanArg);
        if (clan == null) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Clan not found.");
            return 0;
        }
        //plugin.getTeleportManager().teleportToHome(player, clan);
        return 1;
    }

    // /clan mod ban <player>
    private LiteralArgumentBuilder<CommandSourceStack> ban() {
        return Commands.literal("ban")
                .requires(src -> has(src, "simpleclans.mod.ban"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(Suggestions.allPlayers(plugin))
                        .executes(this::execBan));
    }

    private int execBan(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();
        String playerName = StringArgumentType.getString(ctx, "player");
        UUID uuid = cm.getClanPlayer(playerName).getUniqueId();

        if (settings.isBanned(uuid)) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + lang("this.player.is.already.banned", sender));
            return 0;
        }

        cm.ban(uuid);
        ChatBlock.sendMessage(src, ChatFormatting.AQUA + lang("player.added.to.banned.list", sender));

        MinecraftServer server = sender.server;
        ServerPlayer pl = server.getPlayerList().getPlayer(uuid);
        if (pl != null) {
            ChatBlock.sendMessage(pl.createCommandSourceStack(), ChatFormatting.AQUA + lang("you.banned", sender));
        }
        return 1;
    }

    // /clan mod unban <player>
    private LiteralArgumentBuilder<CommandSourceStack> unban() {
        return Commands.literal("unban")
                .requires(src -> has(src, "simpleclans.mod.ban"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(Suggestions.allPlayers(plugin))
                        .executes(this::execUnban));
    }

    private int execUnban(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();
        String playerName = StringArgumentType.getString(ctx, "player");
        UUID uuid = cm.getClanPlayer(playerName).getUniqueId();

        if (!settings.isBanned(uuid)) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + lang("this.player.is.not.banned", sender));
            return 0;
        }

        ServerPlayer pl = sender.server.getPlayerList().getPlayer(uuid);
        if (pl != null) {
            ChatBlock.sendMessage(pl.createCommandSourceStack(),
                    ChatFormatting.AQUA + lang("you.have.been.unbanned.from.clan.commands", sender));
        }

        //settings.removeBanned(uuid);
        ChatBlock.sendMessage(src, ChatFormatting.AQUA + lang("player.removed.from.the.banned.list", sender));
        return 1;
    }

    // /clan mod globalff allow|auto
    private LiteralArgumentBuilder<CommandSourceStack> globalff() {
        return Commands.literal("globalff")
                .requires(src -> has(src, "simpleclans.mod.globalff"))
                .then(Commands.literal("allow").executes(this::execGlobalFfAllow))
                .then(Commands.literal("auto").executes(this::execGlobalFfAuto));
    }

    private int execGlobalFfAllow(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();

        if (settings.is(GLOBAL_FRIENDLY_FIRE)) {
            ChatBlock.sendMessage(src, ChatFormatting.AQUA + lang("global.friendly.fire.is.already.being.allowed", sender));
        } else {
            settings.set(GLOBAL_FRIENDLY_FIRE, true);
            settings.save();
            ChatBlock.sendMessage(src, ChatFormatting.AQUA + lang("global.friendly.fire.is.set.to.allowed", sender));
        }
        return 1;
    }

    private int execGlobalFfAuto(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();

        if (!settings.is(GLOBAL_FRIENDLY_FIRE)) {
            ChatBlock.sendMessage(src, ChatFormatting.AQUA + lang("global.friendy.fire.is.already.being.managed.by.each.clan", sender));
        } else {
            settings.set(GLOBAL_FRIENDLY_FIRE, false);
            settings.save();
            ChatBlock.sendMessage(src, ChatFormatting.AQUA + lang("global.friendy.fire.is.now.managed.by.each.clan", sender));
        }
        return 1;
    }

    // /clan mod verify <clan>
    private LiteralArgumentBuilder<CommandSourceStack> verify() {
        return Commands.literal("verify")
                .requires(src -> has(src, "simpleclans.mod.verify"))
                .then(Commands.argument("clan", StringArgumentType.word())
                        .suggests(Suggestions.Allclans(plugin))
                        .executes(this::execVerify));
    }

    private int execVerify(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();
        String clanArg = StringArgumentType.getString(ctx, "clan");
        Clan clan = resolveClan(clanArg);
        if (clan == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Clan not found.");
            return 0;
        }

        if (!clan.isVerified()) {
            clan.verifyClan();
            clan.addBb(sender.getName().getString(), lang("clan.0.has.been.verified", sender, clan.getName()));
            ChatBlock.sendMessage(src, ChatFormatting.AQUA + lang("the.clan.has.been.verified", sender));
        } else {
            ChatBlock.sendMessage(src, ChatFormatting.RED + lang("the.clan.is.already.verified", sender));
        }
        storage.updateClan(clan);
        return 1;
    }

    // /clan mod kick <player>
    private LiteralArgumentBuilder<CommandSourceStack> kick() {
        return Commands.literal("kick")
                .requires(src -> has(src, "simpleclans.mod.kick"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(Suggestions.allPlayers(plugin))
                        .executes(this::execKick));
    }

    private int execKick(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();
        String playerName = StringArgumentType.getString(ctx, "player");
        UUID uuid = cm.getClanPlayer(playerName).getUniqueId();

        ClanPlayer clanPlayer = cm.getClanPlayer(uuid);
        if (clanPlayer == null || clanPlayer.getClan() == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Player is not in a clan.");
            return 0;
        }

        Clan clan = Objects.requireNonNull(clanPlayer.getClan());
        if (clanPlayer.isLeader() && clan.getLeaders().size() == 1) {
            ChatBlock.sendMessageKey(src, "cannot.kick.last.leader");
            return 0;
        }

        clan.addBb(sender.getName().getString(), lang("has.been.kicked.by", sender, clanPlayer.getName(), sender.getName().getString()));
        clan.removePlayerFromClan(clanPlayer.getUniqueId());
        storage.updateClan(clan);
        return 1;
    }

    // /clan mod disband <clan>
    private LiteralArgumentBuilder<CommandSourceStack> disband() {
        return Commands.literal("disband")
                .requires(src -> has(src, "simpleclans.mod.disband"))
                .then(Commands.argument("clan", StringArgumentType.word())
                        .suggests(Suggestions.Allclans(plugin))
                        .executes(this::execDisband));
    }

    private int execDisband(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();
        String clanArg = StringArgumentType.getString(ctx, "clan");
        Clan clan = resolveClan(clanArg);
        if (clan == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Clan not found.");
            return 0;
        }
        clan.disband(sender.createCommandSourceStack(), true, true);
        return 1;
    }

    // /clan mod rename <clan> <name...>
    private LiteralArgumentBuilder<CommandSourceStack> rename() {
        return Commands.literal("rename")
                .requires(src -> has(src, "simpleclans.mod.rename"))
                .then(Commands.argument("clan", StringArgumentType.word())
                        .suggests(Suggestions.Allclans(plugin))
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(this::execRename)));
    }

    private int execRename(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();

        String clanArg = StringArgumentType.getString(ctx, "clan");
        String name = StringArgumentType.getString(ctx, "name");

        Clan clan = resolveClan(clanArg);
        if (clan == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Clan not found.");
            return 0;
        }

        clan.setName(name);
        storage.updateClan(clan);
        ChatBlock.sendMessageKey(src, "you.have.successfully.renamed.the.clan", name);
        return 1;
    }

    // /clan mod locale <player> <locale>
    private LiteralArgumentBuilder<CommandSourceStack> locale() {
        return Commands.literal("locale")
                .requires(src -> has(src, "simpleclans.mod.locale"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(Suggestions.allPlayers(plugin))
                        .then(Commands.argument("locale", StringArgumentType.word())
                                .executes(this::execLocale)));
    }

    private int execLocale(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();

        String playerName = StringArgumentType.getString(ctx, "player");
        String locale = StringArgumentType.getString(ctx, "locale");

        UUID uuid = cm.getClanPlayer(playerName).getUniqueId();
        ClanPlayer cp = cm.getClanPlayer(uuid);
        if (cp == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Player not found.");
            return 0;
        }

        cp.setLocale(Helper.forLanguageTag(locale.replace("_", "-")));
        storage.updateClanPlayer(cp);
        ChatBlock.sendMessage(src, lang("locale.has.been.changed", sender));
        return 1;
    }

    // ------------------------------------------------------------
    // ADMIN COMMANDS
    // ------------------------------------------------------------

    private LiteralArgumentBuilder<CommandSourceStack> admin() {
        return Commands.literal("admin")
                .requires(src -> has(src, "simpleclans.admin"))
                .then(reload())
                .then(purge())
                .then(promote())
                .then(demote())
                .then(resetkdr())
                .then(permanent());
    }

    // /clan admin reload
    private LiteralArgumentBuilder<CommandSourceStack> reload() {
        return Commands.literal("reload")
                .requires(src -> has(src, "simpleclans.admin.reload"))
                .executes(this::execReload);
    }

    private int execReload(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();

        if (plugin == null) {
            plugin = SimpleClans.getInstance();
        }

        plugin.getStorageManager().saveModified();
        plugin.getSettingsManager().load();
        plugin.getStorageManager().importFromDatabase();
        permissions.loadPermissions();
        NameRulesStore.reload(plugin.getServer());
        ClanConfig.reload(plugin.getServer());

        for (ServerPlayer player: plugin.getServer().getPlayerList().getPlayers()) {
            SCCommandManager.refreshPlayerCommands(player);
        }

        for (Clan clan : cm.getClans()) {
            permissions.updateClanPermissions(clan);
            if (plugin.getChatManager().getDiscordHook(plugin) != null) {
                plugin.getChatManager().getDiscordHook(plugin).updaterolePerms(clan);
            }

        }

        NeoForge.EVENT_BUS.post(new ReloadEvent(sender.createCommandSourceStack()));
        ChatBlock.sendMessage(src, ChatFormatting.AQUA + lang("configuration.reloaded", sender));
        return 1;
    }

    // /clan admin purge <player>
    private LiteralArgumentBuilder<CommandSourceStack> purge() {
        return Commands.literal("purge")
                .requires(src -> has(src, "simpleclans.admin.purge"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(Suggestions.allPlayers(plugin))
                        .executes(this::execPurge));
    }

    private int execPurge(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();

        String playerName = StringArgumentType.getString(ctx, "player");
        UUID uuid = cm.getClanPlayer(playerName).getUniqueId();

        ClanPlayer clanPlayer = cm.getClanPlayer(uuid);
        if (clanPlayer == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Player not found.");
            return 0;
        }

        Clan clan = clanPlayer.getClan();
        if (clan != null && clan.getMembers().size() == 1) {
            clan.disband(sender.createCommandSourceStack(), false, false);
        }

        cm.deleteClanPlayer(clanPlayer);
        ChatBlock.sendMessage(src, ChatFormatting.AQUA + lang("player.purged", sender));
        return 1;
    }

    // /clan admin promote <player>
    private LiteralArgumentBuilder<CommandSourceStack> promote() {
        return Commands.literal("promote")
                .requires(src -> has(src, "simpleclans.admin.promote"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(Suggestions.allPlayers(plugin))
                        .executes(this::execPromote));
    }

    private int execPromote(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();

        String playerName = StringArgumentType.getString(ctx, "player");
        UUID uuid = cm.getClanPlayer(playerName).getUniqueId();

        ClanPlayer clanPlayer = cm.getClanPlayer(uuid);
        if (clanPlayer == null || clanPlayer.getClan() == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Player is not in a clan.");
            return 0;
        }

        if (!permissions.has(sender, "simpleclans.leader.promotable")) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + lang("the.player.does.not.have.the.permissions.to.lead.a.clan", sender));
            return 0;
        }

        Clan clan = Objects.requireNonNull(clanPlayer.getClan());
        if (clan.isLeader(uuid)) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + lang("the.player.is.already.a.leader", sender));
            return 0;
        }

        clan.addBb(sender.getName().getString(), lang("promoted.to.leader", sender, clanPlayer.getName()));
        clan.promote(uuid);
        plugin.getStorageManager().updateClan(clan);
        ChatBlock.sendMessage(src, ChatFormatting.AQUA + lang("player.successfully.promoted", sender));
        return 1;
    }

    // /clan admin demote <player>
    private LiteralArgumentBuilder<CommandSourceStack> demote() {
        return Commands.literal("demote")
                .requires(src -> has(src, "simpleclans.admin.demote"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(Suggestions.allPlayers(plugin))
                        .executes(this::execDemote));
    }

    private int execDemote(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();

        String playerName = StringArgumentType.getString(ctx, "player");
        UUID uuid = cm.getClanPlayer(playerName).getUniqueId();

        ClanPlayer otherCp = cm.getClanPlayer(uuid);
        if (otherCp == null || otherCp.getClan() == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Player is not in a clan.");
            return 0;
        }

        Clan clan = Objects.requireNonNull(otherCp.getClan());
        if (!otherCp.isLeader()) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + lang("player.is.not.a.leader", sender));
            return 0;
        }

        if (clan.getLeaders().size() == 1 && !clan.isPermanent()) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + lang("you.cannot.demote.the.last.leader", sender));
            return 0;
        }

        clan.demote(uuid);
        clan.addBb(sender.getName().getString(), lang("demoted.back.to.member", sender, otherCp.getName()));
        storage.updateClan(clan);
        ChatBlock.sendMessage(src, ChatFormatting.AQUA + lang("player.successfully.demoted", sender));
        return 1;
    }

    // /clan admin resetkdr [everyone|<player>]
    private LiteralArgumentBuilder<CommandSourceStack> resetkdr() {
        return Commands.literal("resetkdr")
                .requires(src -> has(src, "simpleclans.admin.resetkdr"))
                .executes(this::execResetKdrEveryone)
                .then(Commands.literal("everyone").executes(this::execResetKdrEveryone))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(Suggestions.allPlayers(plugin)).executes(this::execResetKdrPlayer));
    }

    private int execResetKdrEveryone(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();

        for (ClanPlayer cp : cm.getAllClanPlayers()) {
            PlayerResetKdrEvent event = new PlayerResetKdrEvent(cp);
            NeoForge.EVENT_BUS.post(event);
            if (!event.isCanceled()) {
                cm.resetKdr(cp);
            }
        }

        ChatBlock.sendMessage(src, ChatFormatting.RED + lang("you.have.reseted.kdr.of.all.players", sender));
        return 1;
    }

    private int execResetKdrPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer sender = src.getPlayerOrException();
        String playerName = StringArgumentType.getString(ctx, "player");
        UUID uuid = cm.getClanPlayer(playerName).getUniqueId();

        ClanPlayer cp = cm.getClanPlayer(uuid);
        if (cp == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Player not found.");
            return 0;
        }

        PlayerResetKdrEvent event = new PlayerResetKdrEvent(cp);
        NeoForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            cm.resetKdr(cp);
            ChatBlock.sendMessage(src, ChatFormatting.RED + lang("you.have.reseted.0.kdr", sender, cp.getName()));
            return 1;
        }
        return 0;
    }

    // /clan admin permanent <clan> <true|false>
    // (dein Bukkit-Code toggelt, ich mache hier toggle ohne bool; optional bool-arg)
    private LiteralArgumentBuilder<CommandSourceStack> permanent() {
        return Commands.literal("permanent")
                .requires(src -> has(src, "simpleclans.admin.permanent"))
                .then(Commands.argument("clan", StringArgumentType.word())
                        .suggests(Suggestions.Allclans(plugin))
                        .executes(this::execPermanentToggle)
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(this::execPermanentSet)));
    }

    private int execPermanentToggle(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer sender = ctx.getSource().getPlayerOrException();
        String clanArg = StringArgumentType.getString(ctx, "clan");
        Clan clan = resolveClan(clanArg);
        if (clan == null) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Clan not found.");
            return 0;
        }

        boolean permanent = !clan.isPermanent();
        clan.setPermanent(permanent);
        clan.addBb(sender.getName().getString(),
                lang(permanent ? "permanent.status.enabled" : "permanent.status.disabled", sender));
        storage.updateClan(clan);
        ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.AQUA + lang("you.have.toggled.permanent.status", sender, clan.getName()));
        return 1;
    }

    private int execPermanentSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer sender = ctx.getSource().getPlayerOrException();
        String clanArg = StringArgumentType.getString(ctx, "clan");
        boolean value = BoolArgumentType.getBool(ctx, "value");

        Clan clan = resolveClan(clanArg);
        if (clan == null) {
            ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Clan not found.");
            return 0;
        }

        clan.setPermanent(value);
        clan.addBb(sender.getName().getString(),
                lang(value ? "permanent.status.enabled" : "permanent.status.disabled", sender));
        storage.updateClan(clan);
        ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.AQUA + lang("you.have.toggled.permanent.status", sender, clan.getName()));
        return 1;
    }

    // ------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------

    private boolean has(CommandSourceStack src, String perm) {
        if (!(src.getEntity() instanceof ServerPlayer sp)) return false;
        return plugin.getPermissionsManager().has(sp, perm);
    }

    private Clan resolveClan(String arg) {
        Clan byName = cm.getClanByName(arg);
        if (byName != null) return byName;
        return cm.getClan(arg);
    }
}
