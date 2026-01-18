package org.emil.hnrpmc.hnclaim.proxy;

import org.emil.hnrpmc.hnclaim.Claim;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.chat.SCMessage;

public interface ProxyManager {

    String getServerName();

    boolean isOnline(String playerName);

    void sendMessage(SCMessage message);

    void sendMessage(String target, String message);

    void sendUpdate(Claim claim);

    void sendDelete(Claim claim);
}

