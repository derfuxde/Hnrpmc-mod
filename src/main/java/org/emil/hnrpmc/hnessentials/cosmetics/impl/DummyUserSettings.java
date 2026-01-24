package org.emil.hnrpmc.hnessentials.cosmetics.impl;




import org.emil.hnrpmc.hnessentials.cosmetics.api.CapeServer;
import org.emil.hnrpmc.hnessentials.cosmetics.api.UserSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DummyUserSettings implements UserSettings {
    private final UUID uuid = UUID.fromString("b1f4a42f-ec33-4608-a8b0-94911d626840");

    @Override
    public long getJoinTime() {
        return 0;
    }

    @Override
    public String getRole() {
        return "default";
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public boolean doHats() {
        return true;
    }

    @Override
    public boolean doShoulderBuddies() {
        return true;
    }

    @Override
    public boolean doBackBlings() {
        return true;
    }

    @Override
    public boolean doLore() {
        return true;
    }

    @Override
    public String getCountryCode() {
        return "AQ";
    }

    @Override
    public boolean hasPerRegionEffects() {
        return false;
    }

    @Override
    public boolean hasPerRegionEffectsSet() {
        return true;
    }

    @Override
    public int getPanorama() {
        return 0;
    }

    @Override
    public Map<String, CapeServer> getCapeServerSettings() {
        return new HashMap<>();
    }

    @Override
    public int getIconSettings() {
        return 0;
    }

    @Override
    public boolean doOnlineActivity() {
        return false;
    }
}
