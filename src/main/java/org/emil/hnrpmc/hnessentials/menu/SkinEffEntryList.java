package org.emil.hnrpmc.hnessentials.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.jetbrains.annotations.NotNull;

public class SkinEffEntryList extends ObjectSelectionList<SkinEffEntry> {
    public String currentcosmetic = "none";
    private Screen parened;
    public SkinEffEntryList(Minecraft minecraft, int width, int height, int top, int itemHeight, String currentcosmetic, Screen parened) {
        super(minecraft, width, height, top, itemHeight);
        this.parened = parened;
        this.currentcosmetic = currentcosmetic;
    }

    public String getCurrentcosmetic() {
        return currentcosmetic;
    }

    public void setCurrentcosmetic(String currentcosmetic) {
        this.currentcosmetic = currentcosmetic;
        if (parened instanceof AdminCosmeticSelect adminScreen) {
            adminScreen.setCosmeticID(currentcosmetic);
        } else if (parened instanceof VIPCosmeticSelect vipScreen) {
            vipScreen.setCosmeticID(currentcosmetic);
        } else {
            HNessentials.getInstance().getLogger().debug("Unbekannter Screen-Typ: " + parened.getClass().getSimpleName());
        }
    }

    public int addEntry(@NotNull SkinEffEntry entry) {
        super.addEntry(entry);
        if (entry.skinEffect.equals(currentcosmetic)) {
            this.setSelected(entry);
        }
        return 1;
    }

    @Override
    public boolean mouseClicked(double p_93420_, double p_93421_, int p_93422_) {
        if (parened instanceof AdminCosmeticSelect adminScreen) {
            adminScreen.setCosmeticID(currentcosmetic);
        } else if (parened instanceof VIPCosmeticSelect vipScreen) {
            vipScreen.setCosmeticID(currentcosmetic);
        } else {
            HNessentials.getInstance().getLogger().debug("Unbekannter Screen-Typ: " + parened.getClass().getSimpleName());
        }
        return super.mouseClicked(p_93420_, p_93421_, p_93422_);
    }
}
