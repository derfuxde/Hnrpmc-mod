package org.emil.hnrpmc.hnessentials.requester;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.Tpa;
import org.emil.hnrpmc.hnessentials.TpaUsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TpaRequester {
    private Map<TpaUsers, Tpa> tpaMap = new HashMap<>();
    private HNessentials plugin;

    public TpaRequester(HNessentials plugin) {
        this.plugin = plugin;
    }

    public void aadTPa(ServerPlayer requester, ServerPlayer receiver, boolean here) {
        Tpa tpa = new Tpa(requester, receiver, here);
        TpaUsers tpaUsers = new TpaUsers(requester.getUUID(), receiver.getUUID());
        tpaMap.put(tpaUsers, tpa);
    }

    public void deleteTPa(ServerPlayer requester, ServerPlayer receiver) {
        TpaUsers tpaUsers = new TpaUsers(requester.getUUID(), receiver.getUUID());
        tpaMap.remove(tpaUsers);
    }

    public Tpa getRequest(TpaUsers tpaUsers) {
        return this.tpaMap.get(tpaUsers);
    }

    public List<Tpa> getRequestsfor(ServerPlayer reciver) {
        List<Tpa> tpaList = new ArrayList<>();
        for (Tpa tpa : tpaMap.values()) {
            if (tpa.getReceiver().getUUID().equals(reciver.getUUID())) {
                tpaList.add(tpa);
            }
        }
        if (tpaList.isEmpty()) return null;
        return tpaList;
    }

    public int onTpaAccept(Tpa tpa) {
        if (tpa == null) return 0;
        TpaUsers tpaUsers = tpa.getTpaUsers();
        if (!tpaMap.containsKey(tpaUsers)) return 0;
        if (tpa.isHere()) {
            ServerPlayer requester = plugin.getServer().getPlayerList().getPlayer(tpaUsers.getRequesterUUID());
            ServerPlayer receiver = plugin.getServer().getPlayerList().getPlayer(tpaUsers.getReceiverUUID());
            if (receiver == null) {
                return 0;
            }
            if (requester == null) {
                receiver.sendSystemMessage(Component.literal("§cDie Tpa ist ungültig"));
                return 0;
            }

            receiver.displayClientMessage(Component.literal("Bitte nicht bewegen du wirst in 3s telepotiert"), true);

            for (final int[] i = {3}; i[0] > 0;){
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                i[0]--;
                                receiver.displayClientMessage(Component.literal("Bitte nicht bewegen du wirst in " + i[0] + "s telepotiert"), true);
                            }
                        },
                        1000
                );
            }


            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            // Code, der nach 3000ms (3s) ausgeführt wird
                            System.out.println("3 Sekunden sind um!");
                        }
                    },
                    3000
            );
            return 1;
        }
        return 1;
    }
}
