package org.emil.hnrpmc.simpleclans.commands.general;

import co.aikar.commands.BaseCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.clan.Suggestions;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.emil.hnrpmc.simpleclans.conversation.*;
import org.emil.hnrpmc.simpleclans.conversation.dings.Convosable;
import org.emil.hnrpmc.simpleclans.conversation.dings.PlayerConvosable;
import org.emil.hnrpmc.simpleclans.events.PlayerResetKdrEvent;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.emil.hnrpmc.simpleclans.managers.PermissionsManager;
import org.emil.hnrpmc.simpleclans.managers.RequestManager;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.emil.hnrpmc.simpleclans.managers.StorageManager;
import org.emil.hnrpmc.simpleclans.ui.InventoryDrawer;
import org.emil.hnrpmc.simpleclans.ui.frames.MainFrame;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.conversation.CreateClanNamePrompt.NAME_KEY;
import static org.emil.hnrpmc.simpleclans.conversation.CreateClanTagPrompt.TAG_KEY;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public class GeneralCommands extends ClanSBaseCommand {

    private final SimpleClans plugin;
    private final ClanManager cm;
    private final SettingsManager settings;
    private final StorageManager storage;
    private final RequestManager requestManager;
    private final PermissionsManager permissions;

    public GeneralCommands(SimpleClans plugin) {
        super(plugin);
        this.plugin = plugin;
        this.cm = plugin.getClanManager();
        this.settings = plugin.getSettingsManager();
        this.storage = plugin.getStorageManager();
        this.requestManager = plugin.getRequestManager();
        this.permissions = plugin.getPermissionsManager();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("");
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(String literal) {
        return Commands.literal(literal)
                .requires(src -> true)
                .executes(ctx -> main(ctx, literal))
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource(), literal)))

                .then(Commands.literal("create")
                        .requires(src -> Conditions.clan(src.getPlayer()) == null)
                        .executes(ctx -> create(ctx.getSource(), null, null))
                        .then(Commands.argument("tag", StringArgumentType.word())
                                .executes(ctx -> create(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "tag"), null))
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> create(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "tag"),
                                                StringArgumentType.getString(ctx, "name"))))))


                .then(Commands.literal("lookup")
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(src -> Conditions.clan(src.getPlayer()) != null)
                                .suggests((ctx, b)  -> (java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions>) (permissions.has(ctx.getSource().getPlayer(), "simpleclans.anyone.lookup") ? Suggestions.allPlayers(plugin) : Suggestions.clanMembersHideMe(plugin)))
                                .executes(ctx -> lookup(ctx, EntityArgument.getPlayer(ctx, "player")))))

                .then(Commands.literal("resetkdr")
                        .requires(src -> Conditions.clan(src.getPlayer()) != null && Conditions.permission(src.getPlayer(), "simpleclans.admin.restetkdr"))
                        .then(Commands.literal("confirm")
                                .executes(ctx -> resetKdrConfirm(ctx.getSource()))));
    }

    private int main(CommandContext<CommandSourceStack> ctx, String literal) {
        CommandSourceStack src = ctx.getSource();

        if (src.getEntity() instanceof ServerPlayer player && settings.is(ENABLE_GUI)) {
            InventoryDrawer.open(player, new MainFrame(player));
            return 1;
        }
        return help(src, literal);
    }

    private int help(CommandSourceStack src, String literal) {
        send(src, Component.literal("Usage:")
                .withStyle(ChatFormatting.YELLOW));
        send(src, Component.literal("/" + literal + "  (GUI wenn möglich)")
                .withStyle(ChatFormatting.GRAY));
        send(src, Component.literal("/" + literal + " create [tag] [name]")
                .withStyle(ChatFormatting.GRAY));
        send(src, Component.literal("/" + literal + " lookup <player>")
                .withStyle(ChatFormatting.GRAY));
        send(src, Component.literal("/" + literal + " resetkdr confirm")
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private int create(CommandSourceStack src, String tag, String name) {
        ServerPlayer player = mustPlayer(src);

        if (!permissions.has(player, "simpleclans.leader.create")) return 0;

        ClanPlayer cp = cm.getAnyClanPlayer(player.getUUID());
        if (cp != null && cp.getClan() != null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + lang("you.must.first.resign", player, cp.getClan().getName()));
            return 0;
        }

        HashMap<Object, Object> initialData = new HashMap<>();
        initialData.put(TAG_KEY, tag);
        initialData.put(NAME_KEY, name);


        SCConversation conversation = new SCConversation(plugin, new PlayerConvosable(player), new CreateClanTagPrompt(), initialData, 60);
        conversation.addConversationCanceller(new RequestCanceller(player.createCommandSourceStack(), ChatFormatting.RED + lang("clan.create.request.cancelled", player)));
        conversation.begin();
        return 1;
    }

    private int lookup(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        CommandSourceStack src = ctx.getSource();

        // Wenn dein Lookup bisher ClanPlayerInput erwartet hat, nimm hier direkt UUID.
        // Passe die Klasse Lookup entsprechend an (UUID ctor) oder bau hier eine kleine Ausgabe.
        var lookup = new org.emil.hnrpmc.simpleclans.commands.data.Lookup(plugin, src, target.getUUID());
        lookup.send();
        return 1;
    }

    private int resetKdrConfirm(CommandSourceStack src) {
        ServerPlayer player = mustPlayer(src);

        if (!permissions.has(player, "simpleclans.vip.resetkdr")) return 0;

        ClanPlayer cp = Objects.requireNonNull(cm.getAnyClanPlayer(player.getUUID()));
        if (!settings.is(ALLOW_RESET_KDR)) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + lang("disabled.command", player));
            return 0;
        }

        PlayerResetKdrEvent event = new PlayerResetKdrEvent(cp);
        NeoForge.EVENT_BUS.post(event);

        if (!event.isCanceled() && cm.purchaseResetKdr(player)) {
            cm.resetKdr(cp);
            ChatBlock.sendMessage(src, ChatFormatting.RED + lang("you.have.reseted.your.kdr", player));
            return 1;
        }
        return 0;
    }

    private int listBalance(CommandSourceStack src) {
        if (src.getEntity() instanceof ServerPlayer player) {
            if (!permissions.has(player, "simpleclans.anyone.list.balance")) return 0;
        }

        List<Clan> clans = cm.getClans();
        if (clans.isEmpty()) {
            send(src, Component.literal(lang("no.clans.have.been.created", src)).withStyle(ChatFormatting.RED));
            return 0;
        }

        send(src, Component.literal(lang("clan.list.balance.header", src,
                settings.getColored(SERVER_NAME), clans.size())));

        String lineFormat = lang("clan.list.balance.line", src);
        String leftBracket = settings.getColored(TAG_BRACKET_COLOR) + settings.getColored(TAG_BRACKET_LEFT);
        String rightBracket = settings.getColored(TAG_BRACKET_COLOR) + settings.getColored(TAG_BRACKET_RIGHT);

        for (int i = 0; i < 10 && i < clans.size(); i++) {
            Clan clan = clans.get(i);
            String name = " " + (clan.isVerified() ? settings.getColored(PAGE_CLAN_NAME_COLOR) : ChatFormatting.GRAY) + clan.getName();
            String line = MessageFormat.format(lineFormat, i + 1, leftBracket, clan.getColorTag(), rightBracket, name);
            send(src, Component.literal(line));
        }

        return 1;
    }

    private static ServerPlayer mustPlayer(CommandSourceStack src) {
        try {
            return src.getPlayerOrException();
        } catch (Exception ex) {
            src.sendFailure(Component.literal("Players only."));
            throw new IllegalStateException("Players only");
        }
    }

    private static void send(CommandSourceStack src, Component message) {
        if (src.getEntity() instanceof ServerPlayer player) {
            player.sendSystemMessage(message);
        } else {
            src.sendSuccess(() -> message, false);
        }
    }
}
