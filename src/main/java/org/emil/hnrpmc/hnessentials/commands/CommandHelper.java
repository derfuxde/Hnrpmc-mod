package org.emil.hnrpmc.hnessentials.commands;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.emil.hnrpmc.hnessentials.HNessentials;

public class CommandHelper {

    public HNessentials plugin;

    public CommandHelper(HNessentials plugin) {
        this.plugin = plugin;
    }

    public String formatMessage(String msg, Object... args) {
        if (!msg.contains("{}")) return msg;
        for (Object arg : args) {
            msg = msg.replaceFirst(java.util.regex.Pattern.quote("{}"), String.valueOf(arg));
        }
        return msg;
    }

    public ServerLevel getLevelByName(String worldName) {
        ResourceLocation location = ResourceLocation.parse(worldName);

        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, location);

        return plugin.getServer().getLevel(key);
    }
}
