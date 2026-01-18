package org.emil.hnrpmc.simpleclans.chat.handlers;

import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.chat.ChatHandler;
import org.emil.hnrpmc.simpleclans.chat.SCMessage;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.emil.hnrpmc.simpleclans.chat.SCMessage.Source;
import static org.emil.hnrpmc.simpleclans.chat.SCMessage.Source.*;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.PERFORMANCE_USE_BUNGEECORD;

/**
 * Handles delivering messages from {@link Source#SPIGOT} or {@link Source#DISCORD} to internal spy chat.
 */
public class SpyChatHandler implements ChatHandler {

    @Override
    public void sendMessage(SCMessage message) {
        ConfigField formatField = ConfigField.valueOf(String.format("%sCHAT_SPYFORMAT",
                message.getSource() == DISCORD ? "DISCORD" : message.getChannel()));
        String format = settingsManager.getString(formatField);
        message.setContent(ChatUtils.stripColors(message.getContent()));
        String formattedMessage = chatManager.parseChatFormat(format, message);

        List<ClanPlayer> onlineSpies = getOnlineSpies();

        // Don't send a duplicate message if a spy is inside the clan
        onlineSpies.removeAll(message.getReceivers());
        onlineSpies.forEach(receiver -> ChatBlock.sendMessage(receiver, formattedMessage));
    }

    @Override
    public boolean canHandle(SCMessage.Source source) {
        return false;
    }

    private List<ClanPlayer> getOnlineSpies() {
        return new ArrayList<>(SimpleClans.getInstance().getServer().getPlayerList().getPlayers()).stream().
                filter(Objects::nonNull).
                filter(player -> permissionsManager.has((ServerPlayer) player, "simpleclans.admin.all-seeing-eye")).
                map(player -> plugin.getClanManager().getCreateClanPlayer(player.getUUID())).
                filter(Objects::nonNull).
                filter(clanPlayer -> !clanPlayer.isMuted()).
                collect(Collectors.toList());
    }
}
