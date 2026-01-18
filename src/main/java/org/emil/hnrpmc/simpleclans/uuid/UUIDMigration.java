package org.emil.hnrpmc.simpleclans.uuid;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 *
 * @author NeT32
 */
public class UUIDMigration {

	private UUIDMigration() {}
	
    public static boolean canReturnUUID() {
        return true;
    }

    @Deprecated
    public static UUID getForcedPlayerUUID(String playerName) {
        Player player = SimpleClans.getInstance().getServer().getPlayerList().getPlayerByName(playerName);

        if (player != null) {
        	return player.getUUID();
        } else {
        	for (ClanPlayer cp : SimpleClans.getInstance().getClanManager().getAllClanPlayers()) {
        		if (cp.getName().equalsIgnoreCase(playerName)) {
        			return cp.getUniqueId();
        		}
        	}
            @SuppressWarnings("deprecation")
            UUID offlinePlayer = SimpleClans.getInstance().getServer().getProfileCache().get(playerName).get().getId();
            return offlinePlayer;
        }
    }

}
