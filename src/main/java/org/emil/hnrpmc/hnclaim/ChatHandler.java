package org.emil.hnrpmc.hnclaim;


import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.chat.SCMessage;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;

public interface ChatHandler {

    HNClaims plugin = HNClaims.getInstance();
    SettingsManager settingsManager = SimpleClans.getInstance().getSettingsManager();

    void sendMessage(SCMessage message);

    boolean canHandle(SCMessage.Source source);
}
