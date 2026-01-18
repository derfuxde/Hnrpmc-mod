package org.emil.hnrpmc.simpleclans.ui;

import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that represents a button in a NeoForge GUI
 */
public abstract class SCComponent {

    // Hinweis: ClickType existiert in Minecraft/NeoForge nicht standardmäßig.
    // Du solltest entweder ein eigenes Enum definieren oder das Click-Event
    // deiner GUI-Library nutzen (z.B. ClickType aus Inventory-Frameworks).

    public record ClickCombination(ClickType type, ClickAction action) {}

    private final HashMap<ClickCombination, Runnable> listeners = new HashMap<>();
    private final HashMap<ClickCombination, Object> permissions = new HashMap<>();
    private final Set<ClickType> verified = new HashSet<>();
    private final Set<ClickType> confirmationRequired = new HashSet<>();
    private @Nullable Object lorePermission;

    @NotNull
    public abstract ItemStack getItem();

    public abstract int getSlot();

    /**
     * In NeoForge gibt es keine ItemMeta mehr.
     * Änderungen werden direkt am ItemStack via DataComponents vorgenommen.
     * Diese Methoden werden in der SCComponentImpl durch direkte Component-Manipulation ersetzt.
     */
    @Deprecated
    public void setItemMeta() {
        // Diese Methode ist in NeoForge nicht mehr sinnvoll,
        // da ItemStack selbst die "Meta" (Components) hält.
    }

    public void setVerifiedOnly(@NotNull ClickType clickType) {
        verified.add(clickType);
    }

    public boolean isVerifiedOnly(@NotNull ClickType clickType) {
        return verified.contains(clickType);
    }

    public void setLorePermission(@Nullable RankPermission permission) {
        lorePermission = permission;
    }

    public void setLorePermission(@Nullable String permission) {
        lorePermission = permission;
    }

    @Nullable
    public Object getLorePermission() {
        return lorePermission;
    }

    public void setPermission(@NotNull ClickType click, @Nullable ClickAction clickAction, @Nullable RankPermission permission) {
        permissions.put(new ClickCombination(click, clickAction), permission);
    }

    public void setPermission(@NotNull ClickType click, @Nullable ClickAction clickAction, @Nullable String permission) {
        permissions.put(new ClickCombination(click, clickAction), permission);
    }

    @Nullable
    public Object getPermission(@NotNull ClickType click, @Nullable ClickAction clickAction) {
        return permissions.get(new ClickCombination(click, clickAction));
    }

    public void setListener(ClickType click, @Nullable ClickAction clickAction, @Nullable Runnable listener) {
        listeners.put(new ClickCombination(click, clickAction), listener);
    }

    @Nullable
    public Runnable getListener(@NotNull ClickType click, @Nullable ClickAction clickAction) {
        return listeners.get(new ClickCombination(click, clickAction));
    }

    public void setConfirmationRequired(@NotNull ClickType click) {
        confirmationRequired.add(click);
    }

    public boolean isConfirmationRequired(@NotNull ClickType click) {
        return confirmationRequired.contains(click);
    }
}