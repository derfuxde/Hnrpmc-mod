package org.emil.hnrpmc.simpleclans.commands.clan;

import com.hypherionmc.sdlink.core.managers.ChannelManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.commands.ClanPlayerInput;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.emil.hnrpmc.simpleclans.conversation.DisbandPrompt;
import org.emil.hnrpmc.simpleclans.conversation.SCConversation;
import org.emil.hnrpmc.simpleclans.conversation.dings.PlayerConvosable;
import org.emil.hnrpmc.simpleclans.hooks.discord.DiscordHook;
import org.emil.hnrpmc.simpleclans.hooks.discord.exceptions.DiscordHookException;
import org.emil.hnrpmc.simpleclans.managers.*;
import org.emil.hnrpmc.simpleclans.utils.CurrencyFormat;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.ChatFormatting.RED;

import java.util.List;
import java.util.Objects;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.events.EconomyTransactionEvent.Cause.DISCORD_CREATION;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public class LeaderCommands extends ClanSBaseCommand {

    private final SimpleClans plugin;
    private final ChatManager chatManager;
    private StorageManager storageManager;
    private final ClanManager clanManager;

    public LeaderCommands(SimpleClans plugin) {
        super(plugin);
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
        this.storageManager = plugin.getStorageManager();
        this.clanManager = plugin.getClanManager();
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

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root)
                .then(demote())
                .then(discordcreate())
                .then(disband())
                .then(verify())
                .then(untrust())
                .then(rename())
                .then(trust())
                .then(promote());
    }

    public LiteralArgumentBuilder<CommandSourceStack> demote() {
        return Commands.literal("demote")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && Conditions.leader(src.getPlayer(), clanManager.getClanByPlayerUniqueId(src.getPlayer().getUUID())))
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(Suggestions.clanMembersHideMe(plugin))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            ServerPlayer otherplayer = EntityArgument.getPlayer(ctx, "player");
                            ClanPlayer otherCp = plugin.getClanManager().getClanPlayer(otherplayer);
                            ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);
                            Clan clan = plugin.getClanManager().getClanByPlayerUniqueId(player.getUUID());
                            if (!clan.enoughLeadersOnlineToDemote(otherCp)) {
                                ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.RED + lang("not.enough.leaders.online.to.vote.on.demotion", player));
                                return 0;
                            }
                            if (!clan.isLeader(otherCp.getUniqueId())) {
                                ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.RED + lang("player.is.not.a.leader.of.your.clan", player));
                                return 0;
                            }
                            if (clan.getLeaders().size() > 2 && plugin.getSettingsManager().is(CLAN_CONFIRMATION_FOR_DEMOTE)) {
                                plugin.getRequestManager().addDemoteRequest(cp, otherCp.getName(), clan);
                                ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.AQUA + lang("demotion.vote.has.been.requested.from.all.leaders",
                                        player));
                                return 0;
                            }
                            clan.addBb(player.getName().getString(), lang("demoted.back.to.member", player, otherCp.getName()));
                            clan.demote(otherCp.getUniqueId());
                            return 1;
                        })
                );

    }

    public LiteralArgumentBuilder<CommandSourceStack> promote() {
        return Commands.literal("promote")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && Conditions.leader(src.getPlayer(), clanManager.getClanByPlayerUniqueId(src.getPlayer().getUUID())))
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(Suggestions.clanMembersHideMe(plugin))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            ServerPlayer otherPl = EntityArgument.getPlayer(ctx, "player");
                            ClanPlayer otherCp = plugin.getClanManager().getClanPlayer(otherPl);
                            ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);
                            Clan clan = plugin.getClanManager().getClanByPlayerUniqueId(player.getUUID());
                            //if (!Conditions.permission(otherPl, "simpleclans.leader.promotable")) {
                                //ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.RED + lang("the.player.does.not.have.the.permissions.to.lead.a.clan",
                                        //player));
                                //return 0;
                            //}
                            if (otherPl.getUUID().equals(player.getUUID())) {
                                ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.RED + lang("you.cannot.promote.yourself", player));
                                return 0;
                            }
                            if (clan.isLeader(otherPl.getUUID())) {
                                ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.RED + lang("the.player.is.already.a.leader", player));
                                return 0;
                            }
                            if (plugin.getSettingsManager().is(CLAN_CONFIRMATION_FOR_PROMOTE) && clan.getLeaders().size() > 1) {
                                plugin.getRequestManager().requestAllLeaders(cp, ClanRequest.PROMOTE, otherPl.getName().getString(), "asking.for.the.promotion",
                                        player.getName(), otherPl.getName());
                                ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.AQUA + lang("promotion.vote.has.been.requested.from.all.leaders",
                                        player));
                                return 0;
                            }

                            clan.addBb(player.getName().getString(), lang("promoted.to.leader", otherPl, otherPl.getName()));
                            clan.promote(otherPl.getUUID());
                            return 1;
                        })
                );

    }

    public LiteralArgumentBuilder<CommandSourceStack> disband() {
        return Commands.literal("disband")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && Conditions.leader(src.getPlayer(), clanManager.getClanByPlayerUniqueId(src.getPlayer().getUUID())))
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);
                        Clan clan = plugin.getClanManager().getClanByPlayerUniqueId(player.getUUID());
                        if (clan.getLeaders().size() != 1) {
                            plugin.getRequestManager().requestAllLeaders(cp, ClanRequest.DISBAND, clan.getTag(), "asking.to.disband",
                                    player.getName());
                            ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.AQUA +
                                    lang("clan.disband.vote.has.been.requested.from.all.leaders", player));
                            return 0;
                        }

                        new SCConversation(plugin, new PlayerConvosable(player), new DisbandPrompt()).begin();
                        return 1;
                    }
                );

    }

    public LiteralArgumentBuilder<CommandSourceStack> verify() {
        return Commands.literal("verify")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && Conditions.leader(src.getPlayer(), clanManager.getClanByPlayerUniqueId(src.getPlayer().getUUID())))
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayer();
                        Clan clan = plugin.getClanManager().getClanByPlayerUniqueId(player.getUUID());
                        if (clan.isVerified()) {
                            ChatBlock.sendMessageKey(player.createCommandSourceStack(), "your.clan.already.verified");
                            return 0;
                        }
                        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_CLAN_VERIFY)) {
                            ChatBlock.sendMessageKey(player.createCommandSourceStack(), "staff.member.verify.clan");
                            return 0;
                        }
                        int minToVerify = plugin.getSettingsManager().getInt(CLAN_MIN_TO_VERIFY);
                        if (minToVerify > clan.getMembers().size()) {
                            ChatBlock.sendMessage(player.createCommandSourceStack(), lang("your.clan.must.have.members.to.verify", player, minToVerify));
                            return 0;
                        }
                        if (clanManager.purchaseVerification(player)) {
                            clan.verifyClan();
                            clan.addBb(player.getName().getString(), lang("clan.0.has.been.verified", player, clan.getName()));
                            ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.AQUA + lang("the.clan.has.been.verified", player));
                            return 1;
                        }
                        return 0;
                    }
                );
    }

    public LiteralArgumentBuilder<CommandSourceStack> trust() {
        return Commands.literal("trust")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && Conditions.leader(src.getPlayer(), clanManager.getClanByPlayerUniqueId(src.getPlayer().getUUID())))
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(Suggestions.clanMembersHideMe(plugin))
                    .executes(ctx -> {
                        ServerPlayer otherpl = EntityArgument.getPlayer(ctx, "player");
                        ClanPlayer trustedInput = clanManager.getClanPlayer(otherpl);
                        ServerPlayer player = ctx.getSource().getPlayer();
                        Clan clan = clanManager.getClanByPlayerUniqueId(player.getUUID());
                        if (player.getUUID().equals(trustedInput.getUniqueId())) {
                            ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.RED + lang("you.cannot.trust.yourself", player));
                            return 0;
                        }
                        if (clan.isLeader(trustedInput.getUniqueId())) {
                            ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.RED + lang("leaders.are.already.trusted", player));
                            return 0;
                        }
                        if (trustedInput.isTrusted()) {
                            ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.RED + lang("this.player.is.already.trusted", player));
                            return 0;
                        }
                        clan.addBb(player.getName().getString(), lang("has.been.given.trusted.status.by", trustedInput.toPlayer(), trustedInput.getName(),
                                player.getName()));
                        trustedInput.setTrusted(true);
                        storageManager.updateClanPlayer(trustedInput);
                        return 1;
                    })
                );
    }

    public LiteralArgumentBuilder<CommandSourceStack> untrust() {
        return Commands.literal("untrust")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && Conditions.leader(src.getPlayer(), clanManager.getClanByPlayerUniqueId(src.getPlayer().getUUID())))
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(Suggestions.clanMembersHideMe(plugin))
                        .executes(ctx -> {
                            ServerPlayer otherpl = EntityArgument.getPlayer(ctx, "player");
                            ClanPlayer trustedInput = clanManager.getClanPlayer(otherpl);
                            ServerPlayer player = ctx.getSource().getPlayer();
                            Clan clan = clanManager.getClanByPlayerUniqueId(player.getUUID());
                            if (trustedInput.getUniqueId().equals(player.getUUID())) {
                                ChatBlock.sendMessage(player.createCommandSourceStack(), RED + lang("you.cannot.untrust.yourself", player));
                                return 0;
                            }
                            if (clan.isLeader(trustedInput.getUniqueId())) {
                                ChatBlock.sendMessage(player.createCommandSourceStack(), RED + lang("leaders.cannot.be.untrusted", player));
                                return 0;
                            }
                            if (!trustedInput.isTrusted()) {
                                ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.RED + lang("this.player.is.already.untrusted", player));
                                return 0;
                            }

                            clan.addBb(player.getName().getString(), lang("has.been.given.untrusted.status.by", player, trustedInput.getName(),
                                    player.getName()));
                            trustedInput.setTrusted(false);
                            storageManager.updateClanPlayer(trustedInput);
                            return 1;
                        })
                );
    }

    public LiteralArgumentBuilder<CommandSourceStack> rename() {
        return Commands.literal("rename")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && Conditions.leader(src.getPlayer(), clanManager.getClanByPlayerUniqueId(src.getPlayer().getUUID())))
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .suggests(Suggestions.clanMembersHideMe(plugin))
                        .executes(ctx -> {
                            String clanName = StringArgumentType.getString(ctx, "name");
                            ServerPlayer player = ctx.getSource().getPlayer();
                            ClanPlayer cp = clanManager.getClanPlayer(player);
                            Clan clan = cp.getClan();
                            if (clanName.contains("&")) {
                                ChatBlock.sendMessageKey(cp, "your.clan.name.cannot.contain.color.codes");
                                return 0;
                            }
                            boolean bypass = plugin.getPermissionsManager().has(player, "simpleclans.mod.bypass");
                            if (!bypass) {
                                if (ChatUtils.stripColors(clanName).length() > plugin.getSettingsManager().getInt(CLAN_MAX_LENGTH)) {
                                    ChatBlock.sendMessage(player.createCommandSourceStack(), RED + lang("your.clan.name.cannot.be.longer.than.characters",
                                            player, plugin.getSettingsManager().getInt(CLAN_MAX_LENGTH)));
                                    return 0;
                                }
                                if (ChatUtils.stripColors(clanName).length() <= plugin.getSettingsManager().getInt(CLAN_MIN_LENGTH)) {
                                    ChatBlock.sendMessage(player.createCommandSourceStack(), RED + lang("your.clan.name.must.be.longer.than.characters",
                                            player, plugin.getSettingsManager().getInt(CLAN_MIN_LENGTH)));
                                    return 0;
                                }
                            }

                            if (clan.getLeaders().size() != 1) {
                                plugin.getRequestManager().requestAllLeaders(cp, ClanRequest.RENAME, clanName, "asking.to.rename", cp.getName(), clanName);
                                ChatBlock.sendMessageKey(cp, "rename.vote.has.been.requested.from.all.leaders");
                                return 0;
                            }

                            clan.setName(clanName);
                            storageManager.updateClan(clan);

                            ChatBlock.sendMessageKey(cp, "you.have.successfully.renamed.your.clan", clanName);
                            return 1;
                        })
                );
    }

    public LiteralArgumentBuilder<CommandSourceStack> discordcreate() {
        return Commands.literal("discord")
                .requires(src -> Conditions.clan(src.getPlayer()) != null && Conditions.leader(src.getPlayer(), clanManager.getClanByPlayerUniqueId(src.getPlayer().getUUID())))
                .then(Commands.literal("create")
                        .executes(ctx -> {
                            plugin.getSettingsManager().load();
                            DiscordHook discordHook = chatManager.getDiscordHook(plugin);
                            ServerPlayer player = ctx.getSource().getPlayer();
                            Clan clan = plugin.getClanManager().getClanByPlayerUniqueId(player.getUUID());
                            if (discordHook == null || ChannelManager.getConsoleChannel() == null) {
                                ChatBlock.sendMessageKey(player.createCommandSourceStack(), "discordhook.is.disabled");
                                return 0;
                            }

                            double amount = plugin.getSettingsManager().getDouble(ECONOMY_DISCORD_CREATION_PRICE);
                            if (plugin.getSettingsManager().is(ECONOMY_PURCHASE_DISCORD_CREATE)) {
                                if (!plugin.getPermissionsManager().playerHasMoney(player.getUUID(), amount)) {
                                    player.sendSystemMessage(Component.literal(ChatFormatting.AQUA + SimpleClans.lang("not.sufficient.money", player, CurrencyFormat.format(amount))));
                                    return 0;
                                }

                                if (!plugin.getPermissionsManager().chargePlayer(player.getUUID(), amount, DISCORD_CREATION)) {
                                    return 0;
                                }
                            }

                            try {
                                discordHook.createChannel(clan.getTag());
                                ChatBlock.sendMessageKey(player.createCommandSourceStack(), "discord.created.successfully");
                                return 1;
                            } catch (DiscordHookException ex) {
                                // Return player's money if clan creation went wrong
                                //if (plugin.getSettingsManager().is(ECONOMY_PURCHASE_DISCORD_CREATE)) {
                                    //plugin.getPermissionsManager().grantPlayer(player, amount, DISCORD_CREATION);
                                //}
                                String messageKey = ex.getMessageKey();
                                if (messageKey != null) {
                                    ChatBlock.sendMessage(player.createCommandSourceStack(), RED + lang(messageKey));
                                    return 0;
                                }
                                return 0;
                            }


                        })
                );
    }
}
