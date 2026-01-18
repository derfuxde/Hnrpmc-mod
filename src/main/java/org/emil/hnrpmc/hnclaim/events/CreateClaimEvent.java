package org.emil.hnrpmc.hnclaim.events;

import net.neoforged.bus.api.Event;
import org.emil.hnrpmc.hnclaim.Claim;

import java.util.*;

import static org.emil.hnrpmc.hnclaim.ChatHandler.plugin;
import static org.emil.hnrpmc.simpleclans.SimpleClans.getInstance;
import static org.emil.hnrpmc.simpleclans.chat.ChatHandler.settingsManager;
import java.util.Map;

public class CreateClaimEvent extends Event {
    private final Claim claim;

    public CreateClaimEvent(Claim claim) {
        this.claim = claim;
        plugin.getLogger().info("Create Claim");

        plugin.getStorageManager().updateClaim(claim);

        plugin.getStorageManager().updateClaim(claim);

        plugin.getStorageManager().updateClaim(claim);
    }

    public Claim getClaim() {
        return claim;
    }
}