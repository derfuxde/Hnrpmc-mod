package org.emil.hnrpmc.simpleclans.chat.handlers;

import org.emil.hnrpmc.simpleclans.chat.ChatHandler;
import org.emil.hnrpmc.simpleclans.chat.SCMessage;
import org.emil.hnrpmc.simpleclans.chat.SCMessage.Source;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.PERFORMANCE_USE_BUNGEECORD;

public class ProxyChatHandler implements ChatHandler {

    @Override
    public void sendMessage(SCMessage message) {
        plugin.getProxyManager().sendMessage(message);
    }

    @Override
    public boolean canHandle(Source source) {
        return source == Source.SERVER && settingsManager.is(PERFORMANCE_USE_BUNGEECORD);
    }
}
