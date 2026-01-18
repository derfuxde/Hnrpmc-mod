package org.emil.hnrpmc.simpleclans.commands.staff;

import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class NoVanishService implements VanishService {

    private final SimpleClans plugin;

    public NoVanishService(SimpleClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<ServerPlayer> getNonVanished(ServerPlayer requester, Clan clan) {
        ClanManager cm = plugin.getClanManager();
        List<ServerPlayer> out = new ArrayList<>();
        for (ClanPlayer cp : clan.getAllMembers()) { // je nach API: clan.getMembers() / getAllMembers()
            ServerPlayer p = cm.getServerPlayer(cp.getUniqueId()); // je nach API bei dir
            if (p != null) out.add(p);
        }
        return out;
    }

    @Override
    public boolean isVanished(ServerPlayer requester, ServerPlayer target) {
        return false;
    }
}
