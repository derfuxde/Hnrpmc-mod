package org.emil.hnrpmc.simpleclans.commands.staff;

import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;

import java.util.List;

public interface VanishService {
    List<ServerPlayer> getNonVanished(ServerPlayer requester, Clan clan);
    boolean isVanished(ServerPlayer requester, ServerPlayer target);
}
