package org.emil.hnrpmc.simpleclans.ui;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SCComponentImpl extends SCComponent {

    @NotNull
    private ItemStack item;
    private int slot;

    // Privater Standard-Konstruktor für den Builder
    private SCComponentImpl() {
        this.item = new ItemStack(Items.STONE);
        this.slot = 0;
    }

    // Konstruktor mit Item (Material)
    public SCComponentImpl(@Nullable String displayName, @Nullable List<String> lore, @NotNull Item material, int slot) {
        this(displayName, lore, new ItemStack(material), slot);
    }

    // Haupt-Konstruktor
    public SCComponentImpl(@Nullable String displayName, @Nullable List<String> lore, @Nullable ItemStack item, int slot) {
        if (item == null) {
            this.item = new ItemStack(Items.STONE);
        } else {
            this.item = item.copy();
        }

        applyData(displayName, lore);
        this.slot = slot;
    }

    // Hilfsmethode, um Redundanz zu vermeiden
    private void applyData(@Nullable String displayName, @Nullable List<String> lore) {
        if (displayName != null) {
            this.item.set(DataComponents.CUSTOM_NAME, Component.literal(displayName));
        }

        if (lore != null && !lore.isEmpty()) {
            List<Component> loreComponents = lore.stream()
                    .map(Component::literal)
                    .collect(Collectors.toList());
            this.item.set(DataComponents.LORE, new ItemLore(loreComponents));
        }

        // Versteckt Attribute/Tooltip (Ersatz für HIDE_ATTRIBUTES)
        //this.item.set(DataComponents.HIDE_TOOLTIP, true);
    }

    @Override
    public @NotNull ItemStack getItem() {
        return item;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    public static class Builder {
        private final SCComponentImpl component = new SCComponentImpl();
        private String displayName;
        private List<String> lore = new ArrayList<>();

        public Builder(@NotNull Item material) {
            this.component.item = new ItemStack(material);
        }

        public Builder(@Nullable ItemStack item) {
            if (item != null) {
                this.component.item = item.copy();
            }
        }

        public Builder withDisplayName(@Nullable String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder withLore(@Nullable List<String> lore) {
            if (lore != null) {
                this.lore = lore;
            }
            return this;
        }

        public Builder withLoreLine(@NotNull String line) {
            this.lore.add(line);
            return this;
        }

        public Builder withSlot(int slot) {
            component.slot = slot;
            return this;
        }

        public SCComponent build() {
            // Wichtig: Hier rufen wir die Komponenten-Logik auf, statt ItemMeta zu nutzen
            component.applyData(this.displayName, this.lore);
            return component;
        }
    }
}