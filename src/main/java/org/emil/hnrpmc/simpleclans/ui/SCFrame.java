package org.emil.hnrpmc.simpleclans.ui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SCFrame {

    private @Nullable SCFrame parent;
    private @NotNull ServerPlayer viewer;
    private final Set<SCComponent> components = ConcurrentHashMap.newKeySet();

    protected SCFrame(@Nullable SCFrame parent, @NotNull ServerPlayer viewer) {
        this.parent = parent;
        this.viewer = viewer;
    }

    @NotNull
    public abstract String getTitle();

    public abstract int getSize();

    public abstract void createComponents();

    @NotNull
    public ServerPlayer getViewer() {
        return viewer;
    }

    public void setViewer(@NotNull ServerPlayer viewer) {
        this.viewer = viewer;
    }

    @Nullable
    public SCFrame getParent() {
        return parent;
    }

    public void setParent(@Nullable SCFrame parent) {
        this.parent = parent;
    }

    @Nullable
    public SCComponent getComponent(int slot) {
        for (SCComponent c : components) {
            if (c.getSlot() == slot) return c;
        }
        return null;
    }

    public void add(@NotNull SCComponent c) {
        components.add(c);
    }

    public void clear() {
        components.clear();
    }

    @NotNull
    public Set<SCComponent> getComponents() {
        return components;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SCFrame otherFrame) {
            return getSize() == otherFrame.getSize()
                    && getTitle().equals(otherFrame.getTitle())
                    && getComponents().equals(otherFrame.getComponents());
        }
        return false;
    }

    public void clicked(int slotId, int button, ClickType clickType, Player player) {

    }

    @Override
    public int hashCode() {
        return getTitle().hashCode() + Integer.hashCode(getSize()) + getComponents().hashCode();
    }
}
