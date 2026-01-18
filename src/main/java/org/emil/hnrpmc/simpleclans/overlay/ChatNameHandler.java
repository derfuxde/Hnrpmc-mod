package org.emil.hnrpmc.simpleclans.overlay;

import com.hypherionmc.sdlink.api.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.api.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.api.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.ServerChatEvent;

import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.config.NameRulesStore;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.chat.ChatHandler.settingsManager;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;
import static org.emil.hnrpmc.simpleclans.overlay.NameDisplayService.pickRule;

@EventBusSubscriber(modid = Hnrpmc.MODID)
public final class ChatNameHandler {

    private static SimpleClans plugin;

    private static SettingsManager settingsManager;

    private static List<UUID> makenametag = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerCommand(CommandEvent event) {
        CommandSourceStack source = event.getParseResults().getContext().getSource();

        // Prüfen, ob der Sender ein Spieler ist
        if ((source.getEntity() instanceof ServerPlayer player)) {
            makenametag.add(player.getUUID());
        }
    }

    private static Component createPlayerHover(ServerPlayer sender, ServerChatEvent event, String formated) {

        String nomsg = formated.replace(event.getMessage().getString(), "");//.trim();
        HoverEvent.EntityTooltipInfo playertip = new HoverEvent.EntityTooltipInfo(EntityType.PLAYER, sender.getUUID(), Component.literal(sender.getName().getString()));
        Component part1 = Component.literal(nomsg)
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + sender.getName().getString() + " "))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, playertip))
                );
        //Component.literal("" + event.getMessage().getString());
        return Component.literal("")
                .append(part1)
                .append(Component.literal("" + event.getRawText().trim()));
    }

    private static void init() {
        if (plugin == null) {
            plugin = SimpleClans.getInstance();
            if (plugin != null) {
                settingsManager = plugin.getSettingsManager();
            }
        }
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        init();
        if (settingsManager.is(CHAT_COMPATIBILITY_MODE) && settingsManager.is(DISPLAY_CHAT_TAGS)) {
            ServerPlayer p = event.getPlayer();
            if (p == null) return;


            event.setCanceled(true);
            //p.sendSystemMessage(Component.literal("Dein Chat ist auf §7diesem Server deaktiviert."));
            String rawname = NameDisplayService.formatForChat(plugin, p);
            String nameFormatted = rawname;
            if (rawname.contains("%message%") || makenametag.contains(p.getUUID())) {
                nameFormatted = rawname;
            } else {
                nameFormatted = rawname + ": %message%";
            }
            String endformat = nameFormatted.replace("%message%", event.getMessage().getString());
            NameRulesStore.Root root = NameRulesStore.get(p.server);
            NameRulesStore.Rule rule = pickRule(plugin, root.rules(), p, "tablist");
            makenametag.remove(p.getUUID());
            if (SDLinkConfig.INSTANCE != null && false) {
                try {
                    String displayname = ClanScoreboard.formatplaceholder(plugin, rule.format(), p);

                    DiscordAuthor author = DiscordAuthor.of(displayname, p.getStringUUID(), p.getGameProfile().getName());
                    DiscordMessage message1 = new DiscordMessageBuilder(MessageType.CHAT)
                            .message(event.getMessage().getString())
                            .author(author)
                            .build();

                    message1.sendMessage();
                } catch (Throwable t) {
                    LOGGER.warn("SDLink not ready, skipping Discord send", t);

                }
            }

            Component msg = createPlayerHover(event.getPlayer(), event, endformat);
            for (ServerPlayer sp : p.getServer().getPlayerList().getPlayers()) {
                sp.sendSystemMessage(msg);
            }
        }
    }
}
