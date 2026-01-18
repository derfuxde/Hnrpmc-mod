package org.emil.hnrpmc.simpleclans.proxy;

import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.chat.SCMessage;

public interface ProxyManager {

    String getServerName();

    boolean isOnline(String playerName);

    void sendMessage(SCMessage message);

    void sendMessage(String target, String message);

    void sendUpdate(Clan clan);

    void sendUpdate(ClanPlayer cp);

    void sendDelete(Clan clan);

    void sendDelete(ClanPlayer cp);
}
