package org.emil.hnrpmc.simpleclans.commands.clan;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BannerItem;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.emil.hnrpmc.simpleclans.managers.*;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public final class ClanCommands extends ClanSBaseCommand {

    private final SimpleClans plugin;
    private final SettingsManager settings;
    private final ClanManager cm;
    private StorageManager storage;
    private final PermissionsManager permissions;
    private final RequestManager requestManager;

    public ClanCommands(SimpleClans plugin) {
        super(plugin);
        this.plugin = plugin;
        this.settings = plugin.getSettingsManager();
        this.cm = plugin.getClanManager();
        this.storage = plugin.getStorageManager();
        this.permissions = plugin.getPermissionsManager();
        this.requestManager = plugin.getRequestManager();
    }

    @Override
    public @Nullable String primarycommand() {
        return "";
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {

        return Commands.literal(root)
                .then(war())
                .then(modtag())
                .then(setbanner())
                .then(invite())
                .then(fee())
                .then(clanff())
                .then(description())
                .then(rival())
                .then(ally())
                .then(kick())
                .then(color())
                .then(resign());
    }

    private LiteralArgumentBuilder<CommandSourceStack> war() {
        return Commands.literal("war")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.WAR_END.getNeoPermission()) || Conditions.rankPermission(src.getPlayer(), RankPermission.WAR_START.getNeoPermission())))
                .then(Commands.literal("start")
                        .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.WAR_START.getNeoPermission())))
                        .then(Commands.argument("clan", StringArgumentType.word())
                                .suggests(Suggestions.clansHideOwn(plugin))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ClanPlayer requester = mustClanPlayer(player);
                                    Clan requestClan = mustClan(player);

                                    Clan targetClan = mustClanByTag(StringArgumentType.getString(ctx, "clan"), ctx.getSource());

                                    if (!hasPermission(ctx.getSource(), "simpleclans.leader.war")) return 0;
                                    if (!Conditions.verified(player)) return 0;
                                    if (!Conditions.rank(requester, "WAR_START")) return 0;
                                    if (!Conditions.canWarTarget(plugin, requester, requestClan, targetClan, ctx.getSource())) return 0;

                                    List<ClanPlayer> onlineLeaders = Helper.stripOffLinePlayers(requestClan.getLeaders());

                                    if (settings.is(WAR_START_REQUEST_ENABLED)) {
                                        if (!onlineLeaders.isEmpty()) {
                                            requestManager.addWarStartRequest(requester, targetClan, requestClan);
                                            ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("leaders.have.been.asked.to.accept.the.war.request", player, targetClan.getName()));
                                        } else {
                                            ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("at.least.one.leader.accept.the.alliance", player));
                                        }
                                    }

                                    return 1;
                                })))
                .then(Commands.literal("end")
                        .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.WAR_END.getNeoPermission())))
                        .then(Commands.argument("clan", StringArgumentType.word())
                        .suggests(Suggestions.warringClans(plugin))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            ClanPlayer cp = mustClanPlayer(player);
                            Clan issuerClan = mustClan(player);

                            if (!hasPermission(ctx.getSource(), "simpleclans.leader.war")) return 0;
                            if (!Conditions.verified(player)) return 0;
                            if (!Conditions.rank(cp, "WAR_END")) return 0;

                            Clan war = mustClanByTag(StringArgumentType.getString(ctx, "clan"), ctx.getSource());

                            if (issuerClan.isWarring(war.getTag())) {
                                requestManager.addWarEndRequest(cp, war, issuerClan);
                                ChatBlock.sendMessage(cp, SimpleClans.lang("leaders.asked.to.end.rivalry", cp.toPlayer(), war.getName()));
                            } else {
                                ChatBlock.sendMessage(cp, SimpleClans.lang("clans.not.at.war", cp.toPlayer()));
                            }

                            return 1;
                        })));
    }

    private LiteralArgumentBuilder<CommandSourceStack> modtag() {
        return Commands.literal("modtag")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.MODTAG.getNeoPermission())))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            Clan clan = mustClan(player);
                            ClanPlayer cp = mustClanPlayer(player);

                            if (!hasPermission(ctx.getSource(), "simpleclans.leader.modtag")) return 0;
                            if (!Conditions.verified(player)) return 0;
                            if (!Conditions.rank(cp, "MODTAG")) return 0;

                            String tag = StringArgumentType.getString(ctx, "tag");

                            TagChangeEvent event = new TagChangeEvent(player, clan, tag);
                            NeoForge.EVENT_BUS.post(event);
                            if (event.isCanceled()) return 0;

                            tag = event.getNewTag();
                            String cleanTag = Helper.cleanTag(tag);

                            Optional<String> validationError = plugin.getTagValidator().validate(player, tag);
                            if (validationError.isPresent()) {
                                ChatBlock.sendMessage(player.createCommandSourceStack(), validationError.get());
                                return 0;
                            }

                            if (!cleanTag.equals(clan.getTag())) {
                                ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("you.can.only.modify.the.color.and.case.of.the.tag", player));
                                return 0;
                            }

                            clan.addBb(player.getName().getString(), SimpleClans.lang("tag.changed.to.0", (ServerPlayer) null, ChatUtils.parseColors(tag)));
                            clan.changeClanTag(tag);
                            cm.updateDisplayName(player);
                            return 1;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> setbanner() {
        return Commands.literal("setbanner")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.MODTAG.getNeoPermission())))

                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    Clan clan = mustClan(player);
                    ClanPlayer cp = mustClanPlayer(player);

                    if (!hasPermission(ctx.getSource(), "simpleclans.leader.setbanner")) return 0;
                    if (!Conditions.verified(player)) return 0;
                    if (!Conditions.rank(cp, "SETBANNER")) return 0;

                    ItemStack hand = player.getMainHandItem();
                    if (!(hand.getItem() instanceof BannerItem)) {
                        ChatBlock.sendMessageKey(player.createCommandSourceStack(), "you.must.hold.a.banner");
                        return 0;
                    }

                    clan.setBanner(hand);
                    if (this.storage == null) {
                        this.storage = plugin.getStorageManager();
                    }
                    storage.updateClan(clan);
                    ChatBlock.sendMessageKey(player.createCommandSourceStack(), "you.changed.clan.banner");
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> invite() {
        return Commands.literal("invite")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.INVITE.getNeoPermission())))
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(Suggestions.nonMembers(plugin))
                        .executes(ctx -> {
                            ServerPlayer sender = ctx.getSource().getPlayerOrException();
                            ServerPlayer invitedPlayer = EntityArgument.getPlayer(ctx, "player");

                            ClanPlayer cp = mustClanPlayer(sender);
                            Clan clan = mustClan(sender);

                            if (!hasPermission(ctx.getSource(), "simpleclans.leader.invite")) return 0;
                            if (!Conditions.rank(cp, "INVITE")) return 0;

                            ClanPlayer invited = plugin.getClanManager().getCreateClanPlayer(invitedPlayer.getUUID());
                            if (invited == null) {
                                ChatBlock.sendMessage(sender.createCommandSourceStack(), "ClanPlayer not found");
                                return 0;
                            }

                            if (!invited.isInviteEnabled()) {
                                ChatBlock.sendMessage(sender.createCommandSourceStack(), SimpleClans.lang("invitedplayer.invite.off", sender));
                                return 0;
                            }
                            if (!permissions.has(invitedPlayer, "simpleclans.member.can-join")) {
                                ChatBlock.sendMessage(sender.createCommandSourceStack(), SimpleClans.lang("the.player.doesn.t.not.have.the.permissions.to.join.clans", sender));
                                return 0;
                            }
                            if (invitedPlayer.getUUID().equals(sender.getUUID())) {
                                ChatBlock.sendMessage(sender.createCommandSourceStack(), SimpleClans.lang("you.cannot.invite.yourself", sender));
                                return 0;
                            }

                            long minutesBeforeRejoin = cm.getMinutesBeforeRejoin(invited, clan);
                            if (minutesBeforeRejoin != 0) {
                                ChatBlock.sendMessage(sender.createCommandSourceStack(), SimpleClans.lang("the.player.must.wait.0.before.joining.your.clan.again", sender, minutesBeforeRejoin));
                                return 0;
                            }

                            int max = settings.getInt(CLAN_MAX_MEMBERS);
                            if (max > 0 && clan.getSize() >= max) {
                                ChatBlock.sendMessage(sender.createCommandSourceStack(), SimpleClans.lang("the.clan.members.reached.limit", sender));
                                return 0;
                            }

                            if (!cm.purchaseInvite(sender)) return 0;

                            requestManager.addInviteRequest(cp, invitedPlayer.getName().getString(), clan);
                            ChatBlock.sendMessage(sender.createCommandSourceStack(), SimpleClans.lang("has.been.asked.to.join", sender, invitedPlayer.getName().getString(), clan.getName()));
                            return 1;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> fee() {
        return Commands.literal("fee")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.FEE_SET.getNeoPermission())) || Conditions.rankPermission(src.getPlayer(), RankPermission.FEE_ENABLE.getNeoPermission()))
                .then(Commands.literal("check")
                        .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.FEE_ENABLE.getNeoPermission())))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            Clan clan = mustClan(player);

                            if (!hasPermission(ctx.getSource(), "simpleclans.member.fee-check")) return 0;
                            if (!Conditions.memberFeeEnabled(plugin, clan, ctx.getSource())) return 0;
                            if (!Conditions.verified(player)) return 0;

                            ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("the.fee.is.0.and.its.current.value.is.1",
                                    player,
                                    clan.isMemberFeeEnabled() ? SimpleClans.lang("fee.enabled", player) : SimpleClans.lang("fee.disabled", player),
                                    clan.getMemberFee()
                            ));
                            return 1;
                        }))
                .then(Commands.literal("set")
                        .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.FEE_SET.getNeoPermission())))
                        .then(Commands.argument("fee", DoubleArgumentType.doubleArg(0D))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    Clan clan = mustClan(player);
                                    ClanPlayer cp = mustClanPlayer(player);

                                    if (!hasPermission(ctx.getSource(), "simpleclans.leader.fee")) return 0;
                                    if (!Conditions.verified(player)) return 0;
                                    if (!Conditions.rank(cp, "FEE_SET")) return 0;
                                    if (!Conditions.changeFee(plugin, player, clan, ctx.getSource())) return 0;

                                    double fee = Math.abs(DoubleArgumentType.getDouble(ctx, "fee"));
                                    double maxFee = settings.getDouble(ECONOMY_MAX_MEMBER_FEE);
                                    if (fee > maxFee) {
                                        return 0;
                                    }

                                    if (cm.purchaseMemberFeeSet(player)) {
                                        clan.setMemberFee(fee);
                                        ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("fee.set", player));
                                        storage.updateClan(clan);
                                        return 1;
                                    }
                                    return 0;
                                })));
    }

    private LiteralArgumentBuilder<CommandSourceStack> clanff() {
        return Commands.literal("clanff")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.FRIENDLYFIRE.getNeoPermission())))
                .then(Commands.literal("allow").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    Clan clan = mustClan(player);
                    ClanPlayer cp = mustClanPlayer(player);

                    if (!hasPermission(ctx.getSource(), "simpleclans.leader.ff")) return 0;
                    if (!Conditions.rank(cp, "FRIENDLYFIRE")) return 0;

                    clan.addBb(player.getName().getString(), SimpleClans.lang("clan.wide.friendly.fire.is.allowed"));
                    clan.setFriendlyFire(true);
                    if (this.storage == null) {
                        this.storage = plugin.getStorageManager();
                    }
                    storage.updateClan(clan);
                    return 1;
                }))
                .then(Commands.literal("block").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    Clan clan = mustClan(player);
                    ClanPlayer cp = mustClanPlayer(player);

                    if (!hasPermission(ctx.getSource(), "simpleclans.leader.ff")) return 0;

                    clan.addBb(player.getName().getString(), SimpleClans.lang("clan.wide.friendly.fire.blocked"));
                    clan.setFriendlyFire(false);
                    if (this.storage == null) {
                        this.storage = plugin.getStorageManager();
                    }
                    storage.updateClan(clan);
                    return 1;
                }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> description() {
        return Commands.literal("description")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.DESCRIPTION.getNeoPermission())))

                .then(Commands.argument("description", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            Clan clan = mustClan(player);
                            ClanPlayer cp = mustClanPlayer(player);

                            if (!hasPermission(ctx.getSource(), "simpleclans.leader.description")) return 0;
                            if (!Conditions.verified(player)) return 0;
                            if (!Conditions.rank(cp, "DESCRIPTION")) return 0;

                            String description = StringArgumentType.getString(ctx, "description");

                            int min = settings.getInt(CLAN_MIN_DESCRIPTION_LENGTH);
                            int max = settings.getInt(CLAN_MAX_DESCRIPTION_LENGTH);

                            if (description.length() < min) {
                                ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("your.clan.description.must.be.longer.than", player, min));
                                return 0;
                            }
                            if (description.length() > max) {
                                ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("your.clan.description.cannot.be.longer.than", player, max));
                                return 0;
                            }

                            clan.setDescription(description);
                            ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("description.changed", player));
                            if (this.storage == null) {
                                this.storage = plugin.getStorageManager();
                            }
                            storage.updateClan(clan);
                            return 1;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> color() {
        return Commands.literal("color")
                .then(Commands.argument("color", StringArgumentType.greedyString())
                        .suggests(Suggestions.clanColors(plugin))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            Clan clan = mustClan(player);
                            String color = StringArgumentType.getString(ctx, "color");
                            ClanPlayer cp = mustClanPlayer(player);

                            if (!hasPermission(ctx.getSource(), "simpleclans.leader.description")) return 0;
                            if (!Conditions.verified(player)) return 0;
                            if (!Conditions.rank(cp, "DESCRIPTION")) return 0;


                            clan.setClanColor(ClanColors.valueOf(color).getColorhex());
                            ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("description.changed", player));
                            if (this.storage == null) {
                                this.storage = plugin.getStorageManager();
                            }
                            storage.updateClan(clan);
                            return 1;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> rival() {
        return Commands.literal("rival")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.RIVAL_REMOVE.getNeoPermission()) || Conditions.rankPermission(src.getPlayer(), RankPermission.RIVAL_ADD.getNeoPermission())))
                .then(Commands.literal("add")
                        .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.RIVAL_ADD.getNeoPermission())))
                        .then(Commands.argument("clan", StringArgumentType.word())
                                .suggests(Suggestions.clansHideOwn(plugin))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    Clan issuerClan = mustClan(player);
                                    ClanPlayer cp = mustClanPlayer(player);

                                    if (!hasPermission(ctx.getSource(), "simpleclans.leader.rival")) return 0;
                                    if (!Conditions.verified(player)) return 0;
                                    if (!Conditions.rivable(plugin, issuerClan, ctx.getSource())) return 0;
                                    if (!Conditions.minimumToRival(plugin, issuerClan, ctx.getSource())) return 0;
                                    if (!Conditions.rank(cp, "RIVAL_ADD")) return 0;

                                    Clan rivalClan = mustClanByTag(StringArgumentType.getString(ctx, "clan"), ctx.getSource());
                                    if (settings.isUnrivable(rivalClan.getTag())) {
                                        ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("the.clan.cannot.be.rivaled", player));
                                        return 0;
                                    }

                                    if (!issuerClan.reachedRivalLimit()) {
                                        if (!issuerClan.isRival(rivalClan.getTag())) {
                                            issuerClan.addRival(rivalClan);
                                            rivalClan.addBb(player.getName().getString(), SimpleClans.lang("has.initiated.a.rivalry", UUID.fromString(issuerClan.getName().toString()), rivalClan.getName()), false);
                                            issuerClan.addBb(player.getName().getString(), SimpleClans.lang("has.initiated.a.rivalry", UUID.fromString(player.getName().getString()), rivalClan.getName()));
                                        } else {
                                            ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("your.clans.are.already.rivals", player));
                                        }
                                    } else {
                                        ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("rival.limit.reached", player));
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("remove")
                        .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.RIVAL_REMOVE.getNeoPermission())))
                        .then(Commands.argument("clan", StringArgumentType.word())
                        .suggests(Suggestions.rivals(plugin))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            ClanPlayer cp = mustClanPlayer(player);
                            Clan issuerClan = mustClan(player);

                            if (!hasPermission(ctx.getSource(), "simpleclans.leader.rival")) return 0;
                            if (!Conditions.verified(player)) return 0;
                            if (!Conditions.rank(cp, "RIVAL_REMOVE")) return 0;

                            Clan rivalClan = mustClanByTag(StringArgumentType.getString(ctx, "clan"), ctx.getSource());
                            if (!Conditions.different(issuerClan, rivalClan, ctx.getSource())) return 0;

                            if (issuerClan.isRival(rivalClan.getTag())) {
                                requestManager.addRivalryBreakRequest(cp, rivalClan, issuerClan);
                                ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("leaders.asked.to.end.rivalry", player, rivalClan.getName()));
                            } else {
                                ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("your.clans.are.not.rivals", player));
                            }
                            return 1;
                        })));
    }

    private LiteralArgumentBuilder<CommandSourceStack> ally() {
        return Commands.literal("ally")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.ALLY_REMOVE.getNeoPermission()) || Conditions.rankPermission(src.getPlayer(), RankPermission.ALLY_ADD.getNeoPermission())))
                .then(Commands.literal("add")
                        .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.ALLY_ADD.getNeoPermission())))
                        .then(Commands.argument("clan", StringArgumentType.word())
                                .suggests(Suggestions.clansHideOwn(plugin))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ClanPlayer cp = mustClanPlayer(player);
                                    Clan issuerClan = mustClan(player);

                                    if (!hasPermission(ctx.getSource(), "simpleclans.leader.ally")) return 0;
                                    if (!Conditions.verified(player)) return 0;
                                    if (!Conditions.rank(cp, "ALLY_ADD")) return 0;
                                    if (!Conditions.minimumToAlly(plugin, issuerClan, ctx.getSource())) return 0;

                                    Clan other = mustClanByTag(StringArgumentType.getString(ctx, "clan"), ctx.getSource());
                                    if (!Conditions.different(issuerClan, other, ctx.getSource())) return 0;

                                    if (issuerClan.isAlly(other.getTag())) {
                                        ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("your.clans.are.already.allies", player));
                                        return 0;
                                    }

                                    int maxAlliances = settings.getInt(CLAN_MAX_ALLIANCES);
                                    if (maxAlliances != -1) {
                                        if (issuerClan.getAllies().size() >= maxAlliances) {
                                            ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("your.clan.reached.max.alliances", player));
                                            return 0;
                                        }
                                        if (other.getAllies().size() >= maxAlliances) {
                                            ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("other.clan.reached.max.alliances", player));
                                            return 0;
                                        }
                                    }

                                    List<ClanPlayer> onlineLeaders = Helper.stripOffLinePlayers(issuerClan.getLeaders());
                                    if (onlineLeaders.isEmpty()) {
                                        ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("at.least.one.leader.accept.the.alliance", player));
                                        return 0;
                                    }

                                    requestManager.addAllyRequest(cp, other, issuerClan);
                                    ChatBlock.sendMessage(player.createCommandSourceStack(), SimpleClans.lang("leaders.have.been.asked.for.an.alliance", player, other.getName()));
                                    return 1;
                                })))
                .then(Commands.literal("remove")
                        .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.ALLY_REMOVE.getNeoPermission())))
                        .then(Commands.argument("clan", StringArgumentType.word())
                        .suggests(Suggestions.alliedClans(plugin))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            Clan issuerClan = mustClan(player);
                            ClanPlayer cp = mustClanPlayer(player);

                            if (!hasPermission(ctx.getSource(), "simpleclans.leader.ally")) return 0;
                            if (!Conditions.verified(player)) return 0;
                            if (!Conditions.rank(cp, "ALLY_REMOVE")) return 0;

                            Clan ally = mustClanByTag(StringArgumentType.getString(ctx, "clan"), ctx.getSource());
                            if (!Conditions.different(issuerClan, ally, ctx.getSource())) return 0;
                            if (!Conditions.alliedClan(issuerClan, ally, ctx.getSource())) return 0;

                            issuerClan.removeAlly(ally);
                            ally.addBb(player.getName().getString(), SimpleClans.lang("has.broken.the.alliance", (ServerPlayer) issuerClan.getName(), ally.getName()), false);
                            issuerClan.addBb(player.getName().getString(), SimpleClans.lang("has.broken.the.alliance", UUID.fromString(player.getName().getString()), ally.getName()));
                            return 1;
                        })));
    }

    private LiteralArgumentBuilder<CommandSourceStack> kick() {
        return Commands.literal("kick")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.KICK.getNeoPermission())))
                .then(Commands.argument("member", EntityArgument.player())
                        .suggests(Suggestions.clanMembersHideOwn(plugin))
                        .executes(ctx -> {
                            ServerPlayer sender = ctx.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "member");

                            if (!Conditions.clanMember(plugin, sender, ctx.getSource())) return 0;

                            ClanPlayer targetCp = plugin.getClanManager().getClanPlayer(target.getUUID());
                            if (targetCp == null) return 0;

                            Clan clan = cm.getClanByPlayerUniqueId(sender.getUUID());
                            if (clan == null) return 0;

                            ClanPlayer senderCp = mustClanPlayer(sender);

                            if (!hasPermission(ctx.getSource(), "simpleclans.leader.kick")) return 0;
                            if (!Conditions.rank(senderCp, "KICK")) return 0;
                            if (!Conditions.sameClan(plugin, sender, target, ctx.getSource())) return 0;

                            if (sender.getUUID().equals(targetCp.getUniqueId())) {
                                ChatBlock.sendMessage(sender.createCommandSourceStack(), SimpleClans.lang("you.cannot.kick.yourself", sender));
                                return 0;
                            }

                            if (clan.isLeader(targetCp.getUniqueId())) {
                                ChatBlock.sendMessage(sender.createCommandSourceStack(), SimpleClans.lang("you.cannot.kick.another.leader", sender));
                                return 0;
                            }

                            clan.addBb(sender.getName().getString(), SimpleClans.lang("has.been.kicked.by", targetCp.toPlayer(), sender.getName().getString(), sender));
                            clan.removePlayerFromClan(targetCp.getUniqueId());
                            return 1;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> resign() {
        return Commands.literal("resign")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID()))))
                .then(Commands.literal("confirm")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            ClanPlayer cp = mustClanPlayer(player);
                            Clan clan = mustClan(player);

                            if (!hasPermission(ctx.getSource(), "simpleclans.member.resign")) return 0;

                            if (clan.isPermanent() || !clan.isLeader(player) || clan.getLeaders().size() > 1) {
                                clan.addBb(player.getName().getString(), SimpleClans.lang("0.has.resigned", player.getUUID()));
                                cp.addResignTime(clan.getTag());
                                clan.removePlayerFromClan(player.getUUID());
                                ChatBlock.sendMessage(cp, SimpleClans.lang("resign.success", player));
                            } else if (clan.isLeader(player) && clan.getLeaders().size() == 1) {
                                clan.disband(player.createCommandSourceStack(), true, false);
                                ChatBlock.sendMessage(cp, SimpleClans.lang("clan.has.been.disbanded", player, clan.getName()));
                            } else {
                                ChatBlock.sendMessage(cp, SimpleClans.lang("last.leader.cannot.resign.you.must.appoint.another.leader.or.disband.the.clan", player));
                            }
                            return 1;
                        }))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (!Conditions.clanMember(plugin, player, ctx.getSource())) return 0;
                    if (!hasPermission(ctx.getSource(), "simpleclans.member.resign")) return 0;

                    ChatBlock.sendMessage(player.createCommandSourceStack(), "Type /clan resign confirm to leave.");
                    return 1;
                });
    }

    // --- helpers ---

    private boolean hasPermission(CommandSourceStack src, String perm) {
        return permissions.has(src.getPlayer(), perm);
    }

    private Clan mustClan(ServerPlayer player) {
        Clan c = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(player.getUUID());
        if (c == null) throw new IllegalStateException("Player not in clan");
        return c;
    }

    private ClanPlayer mustClanPlayer(ServerPlayer player) {
        ClanPlayer cp = SimpleClans.getInstance().getClanManager().getCreateClanPlayer(player.getUUID());
        if (cp == null) throw new IllegalStateException("ClanPlayer missing");
        return cp;
    }

    private Clan mustClanByTag(String tagOrName, CommandSourceStack src) {
        Clan c = SimpleClans.getInstance().getClanManager().getClan(tagOrName);
        if (c == null) {
            src.sendFailure(Component.translatable("simpleclans.command.clan_not_found", tagOrName));
            throw new IllegalStateException("Clan not found");
        }
        return c;
    }
}
