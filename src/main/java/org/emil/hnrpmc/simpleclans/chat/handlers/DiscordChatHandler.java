package org.emil.hnrpmc.simpleclans.chat.handlers;

import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.chat.ChatHandler;
import org.emil.hnrpmc.simpleclans.chat.SCMessage;
import org.emil.hnrpmc.simpleclans.hooks.discord.DiscordHook;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

import static org.emil.hnrpmc.simpleclans.ClanPlayer.Channel.CLAN;
import static org.emil.hnrpmc.simpleclans.chat.SCMessage.Source;
import static org.emil.hnrpmc.simpleclans.chat.SCMessage.Source.SERVER;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.DISCORDCHAT_FORMAT_TO;

/**
 * Handles delivering messages from {@link Source#SERVER} to {@link Source#DISCORD}.
 */
public class DiscordChatHandler implements ChatHandler {

    @Override
    public void sendMessage(@NotNull SCMessage message) {
        if (message.getChannel() != CLAN) {
            return;
        }

        String format = settingsManager.getString(DISCORDCHAT_FORMAT_TO);
        String formattedMessage = ChatUtils.stripColors(chatManager.parseChatFormat(format, message));

        Clan clan = message.getSender().getClan();
        if (clan == null) {
            return;
        }

        DiscordHook discordHook = Objects.requireNonNull(chatManager.getDiscordHook(plugin), "DiscordHook cannot be null");
        Optional<TextChannel> channel = discordHook.getCachedChannel(clan.getTag());
        channel.ifPresent(textChannel -> textChannel.sendMessage(formattedMessage));
    }

    @Override
    public boolean canHandle(SCMessage.Source source) {
        return source == SERVER && chatManager.isDiscordHookEnabled(plugin);
    }
}
