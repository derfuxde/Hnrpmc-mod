package org.emil.hnrpmc.doc.menu.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import org.emil.hnrpmc.doc.menu.MainMenu;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.menu.AdminCosmeticSelect;
import org.emil.hnrpmc.hnessentials.menu.CosmeticEntry;
import org.emil.hnrpmc.hnessentials.menu.VIPCosmeticSelect;
import org.jetbrains.annotations.NotNull;

public class DocList extends ObjectSelectionList<DocEntry> {
    private MainMenu parened;
    public DocList(Minecraft minecraft, int width, int height, int top, int itemHeight, MainMenu parened) {
        super(minecraft, width, height, top, itemHeight);
        this.parened = parened;
    }

    public int addEntry(@NotNull DocEntry entry) {
        super.addEntry(entry);
        return 1;
    }

    public MainMenu getParened() {
        return parened;
    }
}
