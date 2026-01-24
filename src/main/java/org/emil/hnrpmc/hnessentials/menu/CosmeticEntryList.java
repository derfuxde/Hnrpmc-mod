package org.emil.hnrpmc.hnessentials.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import org.jetbrains.annotations.NotNull;

public class CosmeticEntryList extends ObjectSelectionList<CosmeticEntry> {
    public CosmeticEntryList(Minecraft minecraft, int width, int height, int top, int itemHeight) {
        super(minecraft, width, height, top, itemHeight);
    }

    public int addEntry(@NotNull CosmeticEntry entry) {
        super.addEntry(entry);
        return 1;
    }
}
