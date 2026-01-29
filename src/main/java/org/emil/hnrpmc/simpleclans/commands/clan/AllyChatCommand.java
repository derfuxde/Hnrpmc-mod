package org.emil.hnrpmc.simpleclans.commands.clan;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.emil.hnrpmc.simpleclans.managers.ChatManager;
import org.emil.hnrpmc.simpleclans.managers.PermissionsManager;
import org.emil.hnrpmc.simpleclans.managers.StorageManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.emil.hnrpmc.simpleclans.ClanPlayer.Channel.ALLY;
import static org.emil.hnrpmc.simpleclans.ClanPlayer.Channel.NONE;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.chat.SCMessage.Source.SERVER;

public final class AllyChatCommand extends ClanSBaseCommand {

    private final SimpleClans plugin;
    private final ChatManager chatManager;
    private StorageManager storageManager;
    private final PermissionsManager permissions;

    public @Nullable List<String> primarycommand() {
        return List.of("ally");
    }


    public AllyChatCommand(SimpleClans plugin) {
        super(plugin);
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
        this.storageManager = plugin.getStorageManager();
        this.permissions = plugin.getPermissionsManager();
    }

    @Override
    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String literal) {
        dispatcher.register(root(literal));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(String literal) {
        return Commands.literal(literal)
                .requires(src -> Conditions.clan(src.getPlayer()) != null && !Conditions.clan(src.getPlayer()).getAllies().isEmpty() && ( Conditions.leader(src.getPlayer(), plugin.getClanManager().getClanByPlayerUniqueId(src.getPlayer().getUUID())) || Conditions.rankPermission(src.getPlayer(), RankPermission.ALLY_CHAT.getNeoPermission())))
                .executes(ctx -> {
                    // /allychat -> Hilfe/Usage
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    ChatBlock.sendMessage(player.createCommandSourceStack(),
                            "Usage: /" + literal + " <message> | join | leave | mute");
                    return 1;
                })
                .then(Commands.literal("join").executes(ctx -> join(ctx.getSource())))
                .then(Commands.literal("leave").executes(ctx -> leave(ctx.getSource())))
                .then(Commands.literal("mute").executes(ctx -> mute(ctx.getSource())))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> sendMessage(ctx.getSource(), StringArgumentType.getString(ctx, "message"))));
    }

    private int sendMessage(CommandSourceStack src, String message) {
        ServerPlayer player = mustPlayer(src);
        ClanPlayer cp = mustClanPlayer(player, src);

        if (!permissions.has(player, "simpleclans.member.ally")) return 0;
        if (!canUseAllyChat(cp, src)) return 0;

        chatManager.processChat(SERVER, ALLY, cp, message);
        return 1;
    }

    private int join(CommandSourceStack src) {
        ServerPlayer player = mustPlayer(src);
        ClanPlayer cp = mustClanPlayer(player, src);

        if (!permissions.has(player, "simpleclans.member.ally")) return 0;
        if (!canUseAllyChat(cp, src)) return 0;

        if (cp.getChannel() == ALLY) {
            ChatBlock.sendMessage(src, lang("already.joined.ally.chat", player));
            return 0;
        }

        cp.setChannel(ALLY);
        if(this.storageManager == null) {
            storageManager = plugin.getStorageManager();
        }
        storageManager.updateClanPlayer(cp);
        ChatBlock.sendMessage(src, lang("joined.ally.chat", player));
        return 1;
    }

    private int leave(CommandSourceStack src) {
        ServerPlayer player = mustPlayer(src);
        ClanPlayer cp = mustClanPlayer(player, src);

        if (!permissions.has(player, "simpleclans.member.ally")) return 0;

        if (cp.getChannel() == ALLY) {
            cp.setChannel(NONE);
            if(this.storageManager == null) {
                storageManager = plugin.getStorageManager();
            }
            storageManager.updateClanPlayer(cp);
            ChatBlock.sendMessage(src, lang("left.ally.chat", player));
            return 1;
        }

        ChatBlock.sendMessage(src, lang("chat.didnt.join", player));
        return 0;
    }

    private int mute(CommandSourceStack src) {
        ServerPlayer player = mustPlayer(src);
        ClanPlayer cp = mustClanPlayer(player, src);

        if (!permissions.has(player, "simpleclans.member.ally")) return 0;
        if (!canUseAllyChat(cp, src)) return 0;

        boolean newState = !cp.isMutedAlly();
        cp.mute(ALLY, newState);
        if(this.storageManager == null) {
            storageManager = plugin.getStorageManager();
        }
        storageManager.updateClanPlayer(cp);

        ChatBlock.sendMessage(src, newState
                ? lang("muted.ally.chat", player)
                : lang("unmuted.ally.chat", player));
        return 1;
    }

    private boolean canUseAllyChat(ClanPlayer cp, CommandSourceStack src) {
        // Port deiner alten Conditions:
        // - clan_member: ist hier durch mustClanPlayer bereits erfüllt
        // - can_chat:type=ALLY: z.B. SettingsManager.ALLYCHAT_ENABLE oder cp muted etc.
        // - rank:name=ALLY_CHAT: cp muss RankPermission ALLY_CHAT besitzen (oder Leader/Trusted)
        //
        // Hier minimal implementiert: Settings + Rank.
        if (!plugin.getSettingsManager().is(org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.ALLYCHAT_ENABLE)) {
            ChatBlock.sendMessage(src, lang("ally.chat.disabled", (ServerPlayer) null));
            return false;
        }
        // Wenn du ein RankPermission-System hast, nutze das:
        // if (!plugin.getPermissionsManager().has(player, RankPermission.ALLY_CHAT, true)) return false;
        // Alternativ, wenn du über Rank-Strings gehst:
        // if (!Conditions.rank(cp, "ALLY_CHAT")) ...
        return true;
    }

    private static ServerPlayer mustPlayer(CommandSourceStack src) {
        try {
            return src.getPlayerOrException();
        } catch (Exception ex) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("Players only."));
            throw new IllegalStateException("Players only");
        }
    }

    private ClanPlayer mustClanPlayer(ServerPlayer player, CommandSourceStack src) {
        ClanPlayer cp = plugin.getClanManager().getClanPlayer(player.getUUID());
        if (cp == null || cp.getClan() == null) {
            ChatBlock.sendMessage(src, lang("not.a.member.of.any.clan", player));
            throw new IllegalStateException("Not in clan");
        }
        return cp;
    }
}
