package org.emil.hnrpmc.simpleclans.chat.handlers;

import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.chat.ChatHandler;
import org.emil.hnrpmc.simpleclans.chat.SCMessage;
import org.emil.hnrpmc.simpleclans.events.ChatEvent;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;

import static org.emil.hnrpmc.simpleclans.chat.SCMessage.Source.*;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.PERFORMANCE_USE_BUNGEECORD;

@SuppressWarnings("unused")
public class ServerChatHandler implements ChatHandler {

    @Override
    public void sendMessage(SCMessage message) {
        /*
          TODO: Make it async, change Type to Channel in 3.0
        */
        plugin.getServer().execute(() -> {
            // 1. Event erstellen und werfen
            // In NeoForge nutzt man den NeoForge.EVENT_BUS anstelle von getPluginManager().callEvent()
            ChatEvent event = new ChatEvent(
                    message.getContent(),
                    message.getSender(),
                    message.getReceivers(),
                    ChatEvent.Type.valueOf(message.getChannel().name())
            );

            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);

            if (event.isCanceled()) { // NeoForge nutzt isCanceled() statt isCancelled()
                return;
            }

            // 2. Nachricht verarbeiten
            message.setContent(stripColorsAndFormatsPerPermission(message.getSender(), event.getMessage()));

            // Format aus der Config laden
            String configKey = String.format("%sCHAT_FORMAT",
                    message.getSource() == org.emil.hnrpmc.simpleclans.chat.SCMessage.Source.DISCORD ? "DISCORD" : message.getChannel());

            // Annahme: Dein SettingsManager hat eine Methode, um via String-Key zuzugreifen
            String format = settingsManager.getString(org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.valueOf(configKey));
            String formattedMessage = chatManager.parseChatFormat(format, message, event.getPlaceholders());

            // 3. Logging (SLF4J Logger von NeoForge)
            plugin.getLogger().info(ChatUtils.stripColors(formattedMessage));

            // 4. Nachricht an alle Empfänger senden
            for (ClanPlayer cp : message.getReceivers()) {
                // ChatBlock.sendMessage muss für NeoForge angepasst sein (nutzt intern player.sendSystemMessage)
                ChatBlock.sendMessage(cp, formattedMessage);
            }
        });
    }

    private String stripColorsAndFormatsPerPermission(ClanPlayer sender, String message) {
        if (!permissionsManager.has(sender.toPlayer(), "simpleclans.member.chat.color")) {
            message = stripColors(message);
        }
        if (!permissionsManager.has(sender.toPlayer(), "simpleclans.member.chat.format")) {
            message = stripFormats(message);
        }
        return message;
    }

    private String stripColors(String message) {
        return message.replaceAll("[§&][0-9a-fA-FxX]", "");
    }

    private String stripFormats(String message) {
        return message.replaceAll("[§&][k-orK-OR]", "");
    }

    @Override
    public boolean canHandle(SCMessage.Source source) {
        return source == SERVER || (source == PROXY && settingsManager.is(PERFORMANCE_USE_BUNGEECORD))
                || (source == DISCORD && chatManager.isDiscordHookEnabled(plugin));
    }
}
