package org.emil.hnrpmc.simpleclans.listeners;

import net.minecraft.world.entity.Entity;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class SCListener {

    protected final SimpleClans plugin;

    public SCListener(SimpleClans plugin) {
        this.plugin = plugin;
    }

    public boolean isBlacklistedWorld(@NotNull Entity entity) {
        List<String> words = plugin.getSettingsManager().getStringList(ConfigField.BLACKLISTED_WORLDS);

        if (words.contains(entity.level().dimension().location().toString())) {
            SimpleClans.debug("Blacklisted world");
            return true;
        }
        return false;
    }

}
