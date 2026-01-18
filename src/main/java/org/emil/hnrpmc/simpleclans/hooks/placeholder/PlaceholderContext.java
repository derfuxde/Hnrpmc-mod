package org.emil.hnrpmc.simpleclans.hooks.placeholder;

import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlaceholderContext {
    private final SimpleClans plugin;
    private final @Nullable ServerPlayer player;
    private final @NotNull Object object;

    public PlaceholderContext(SimpleClans plugin, @Nullable ServerPlayer player) {
        this(plugin, player, player != null ? player : new Object());
    }

    public PlaceholderContext(SimpleClans plugin, @Nullable ServerPlayer player, @NotNull Object object) {
        this.plugin = plugin;
        this.player = player;
        this.object = object;
    }

    public SimpleClans plugin() {
        return plugin;
    }

    public @Nullable ServerPlayer player() {
        return player;
    }

    public @NotNull Object object() {
        return object;
    }
}