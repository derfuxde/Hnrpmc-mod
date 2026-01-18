package org.emil.hnrpmc.simpleclans.chat;

import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.ChatManager;
import org.emil.hnrpmc.simpleclans.managers.PermissionsManager;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;

public interface ChatHandler {

    SimpleClans plugin = SimpleClans.getInstance();
    SettingsManager settingsManager = plugin.getSettingsManager();
    ChatManager chatManager = plugin.getChatManager();
    PermissionsManager permissionsManager = plugin.getPermissionsManager();

    void sendMessage(SCMessage message);

    boolean canHandle(SCMessage.Source source);
}
