package org.emil.hnrpmc.simpleclans.commands.clan;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.emil.hnrpmc.simpleclans.managers.ChatManager;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.emil.hnrpmc.simpleclans.managers.StorageManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.emil.hnrpmc.simpleclans.ClanPlayer.Channel.CLAN;
import static org.emil.hnrpmc.simpleclans.ClanPlayer.Channel.NONE;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.chat.SCMessage.Source.SERVER;

public final class ChatCommand extends ClanSBaseCommand {

    private final SimpleClans plugin;
    private final ChatManager chatManager;
    private StorageManager storageManager;
    private final ClanManager clanManager;

    public ChatCommand(SimpleClans plugin) {
        super(plugin);
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
        this.storageManager = plugin.getStorageManager();
        this.clanManager = plugin.getClanManager();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("clan", ".");
    }

    @Override
    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(rootLiteral.equals(".") ? root2(dispatcher, rootLiteral) : root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root)
                .requires(src -> Conditions.clan(src.getPlayer()) != null )
                .then(chat());
    }

    private LiteralArgumentBuilder<CommandSourceStack> root2(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root)
                .requires(src -> Conditions.clan(src.getPlayer()) != null )
                .executes(ctx -> {
                    // wie ACF @Default ohne message -> typischerweise Usage anzeigen
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (!basicConditions(player, ctx.getSource())) return 0;
                    if (!hasPerm(player, "simpleclans.member.chat")) return 0;
                    if (!clanMember(player, ctx.getSource())) return 0;
                    if (!canChat(player, ctx.getSource(), CLAN)) return 0;

                    ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Usage: /. <message>");
                    return 0;
                })
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String message = StringArgumentType.getString(ctx, "message");

                            if (!basicConditions(player, ctx.getSource())) return 0;
                            if (!hasPerm(player, "simpleclans.member.chat")) return 0;
                            ClanPlayer cp = mustClanPlayer(player, ctx.getSource());
                            if (cp == null) return 0;
                            if (!canChat(player, ctx.getSource(), CLAN)) return 0;

                            chatManager.processChat(SERVER, CLAN, cp, message);
                            return 1;
                        }))
                .then(join())
                .then(leave())
                .then(mute());
    }

    /**
     * Register unter /clan chat ...
     */
    public LiteralArgumentBuilder<CommandSourceStack> chat() {
        return Commands.literal("chat")
                .requires(src -> src.getEntity() instanceof ServerPlayer)
                .executes(ctx -> {
                    // wie ACF @Default ohne message -> typischerweise Usage anzeigen
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (!basicConditions(player, ctx.getSource())) return 0;
                    if (!hasPerm(player, "simpleclans.member.chat")) return 0;
                    if (!clanMember(player, ctx.getSource())) return 0;
                    if (!canChat(player, ctx.getSource(), CLAN)) return 0;

                    ChatBlock.sendMessage(ctx.getSource(), ChatFormatting.RED + "Usage: /clan chat <message>");
                    return 0;
                })
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String message = StringArgumentType.getString(ctx, "message");

                            if (!basicConditions(player, ctx.getSource())) return 0;
                            if (!hasPerm(player, "simpleclans.member.chat")) return 0;
                            ClanPlayer cp = mustClanPlayer(player, ctx.getSource());
                            if (cp == null) return 0;
                            if (!canChat(player, ctx.getSource(), CLAN)) return 0;

                            chatManager.processChat(SERVER, CLAN, cp, message);
                            return 1;
                        }))
                .then(join())
                .then(leave())
                .then(mute());
    }

    private LiteralArgumentBuilder<CommandSourceStack> join() {
        return Commands.literal("join")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    if (!basicConditions(player, ctx.getSource())) return 0;
                    if (!hasPerm(player, "simpleclans.member.chat")) return 0;

                    ClanPlayer cp = mustClanPlayer(player, ctx.getSource());
                    if (cp == null) return 0;
                    if (!canChat(player, ctx.getSource(), CLAN)) return 0;

                    if (cp.getChannel() == CLAN) {
                        ChatBlock.sendMessage(ctx.getSource(), lang("already.joined.clan.chat", player));
                        return 0;
                    }

                    cp.setChannel(CLAN);
                    if (storageManager == null) {
                        this.storageManager = plugin.getStorageManager();
                    }
                    storageManager.updateClanPlayer(cp);
                    ChatBlock.sendMessage(ctx.getSource(), lang("joined.clan.chat", player));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> leave() {
        return Commands.literal("leave")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    if (!basicConditions(player, ctx.getSource())) return 0;
                    if (!hasPerm(player, "simpleclans.member.chat")) return 0;

                    ClanPlayer cp = mustClanPlayer(player, ctx.getSource());
                    if (cp == null) return 0;

                    if (cp.getChannel() == CLAN) {
                        cp.setChannel(NONE);
                        if (storageManager == null) {
                            this.storageManager = plugin.getStorageManager();
                        }
                        storageManager.updateClanPlayer(cp);
                        ChatBlock.sendMessage(ctx.getSource(), lang("left.clan.chat", player));
                        return 1;
                    }

                    ChatBlock.sendMessage(ctx.getSource(), lang("chat.didnt.join", player));
                    return 0;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> mute() {
        return Commands.literal("mute")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    if (!basicConditions(player, ctx.getSource())) return 0;
                    if (!hasPerm(player, "simpleclans.member.chat")) return 0;

                    ClanPlayer cp = mustClanPlayer(player, ctx.getSource());
                    if (cp == null) return 0;

                    if (storageManager == null) {
                        this.storageManager = plugin.getStorageManager();
                    }
                    if (!cp.isMuted()) {
                        cp.mute(CLAN, true);
                        storageManager.updateClanPlayer(cp);
                        ChatBlock.sendMessage(ctx.getSource(), lang("muted.clan.chat", player));
                    } else {
                        cp.mute(CLAN, false);
                        storageManager.updateClanPlayer(cp);
                        ChatBlock.sendMessage(ctx.getSource(), lang("unmuted.clan.chat", player));
                    }
                    return 1;
                });
    }

    // ---------------- helpers (ACF Conditions Ersatz) ----------------

    private boolean hasPerm(ServerPlayer player, String perm) {
        return plugin.getPermissionsManager().has(player, perm);
    }

    private boolean basicConditions(ServerPlayer player, CommandSourceStack src) {
        // Falls du Verified/WorldBlacklist/etc. hier bündeln willst.
        return true;
    }

    private boolean clanMember(ServerPlayer player, CommandSourceStack src) {
        ClanPlayer cp = clanManager.getClanPlayer(player.getUUID());
        if (cp == null || cp.getClan() == null) {
            ChatBlock.sendMessage(src, lang("not.a.member.of.any.clan", player));
            return false;
        }
        return true;
    }

    private boolean canChat(ServerPlayer player, CommandSourceStack src, ClanPlayer.Channel type) {
        // Hier deine alte can_chat Condition abbilden:
        // z.B. Settings: CLANCHAT_ENABLE / Mute-Status / Chat-Spy / Verified usw.
        return true;
    }

    private ClanPlayer mustClanPlayer(ServerPlayer player, CommandSourceStack src) {
        ClanPlayer cp = clanManager.getClanPlayer(player.getUUID());
        if (cp == null) {
            ChatBlock.sendMessage(src, lang("not.a.member.of.any.clan", player));
            return null;
        }
        return cp;
    }
}
