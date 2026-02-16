package org.emil.hnrpmc.doc.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.emil.hnrpmc.doc.HNDoc;
import org.emil.hnrpmc.doc.managers.Loader;
import org.emil.hnrpmc.doc.menu.components.DocEntry;
import org.emil.hnrpmc.doc.menu.components.DocList;
import org.emil.hnrpmc.doc.menu.components.TextScreen;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CustomCosmetic;
import org.emil.hnrpmc.hnessentials.menu.CosmeticEntry;
import org.emil.hnrpmc.hnessentials.menu.CosmeticEntryList;
import org.emil.hnrpmc.hnessentials.network.CosmeticRegistry;

import java.util.*;
import java.util.function.Supplier;

public class MainMenu extends Screen {

    private DocList list;
    public TextScreen currentText;

    public int GUIwidth = (this.width / 2) + 300;

    public MainMenu(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        //GUIwidth = this.width;// - 50;
        GUIwidth = this.width - 10;
        int centerX = GUIwidth / 2;

        this.list = new DocList(minecraft, centerX - (GUIwidth / 2) + 120, this.height - 30, 15, 20, this);

        this.list.setX(centerX - (GUIwidth / 2) + 10);

        Loader loader = HNDoc.getInstance().getLoader();

        for (String directoryPath : loader.Paths.keySet()) {
            String dirDisplayName = directoryPath.contains("/")
                    ? directoryPath.substring(directoryPath.lastIndexOf("/") + 1)
                    : directoryPath;

            if (dirDisplayName.isEmpty()) continue;

            // Ordner-Eintrag erstellen (Vielleicht willst du hier ein Icon hinzufügen?)
            DocEntry dirEntry = new DocEntry(Component.literal("§6> " + dirDisplayName), minecraft, this.list, directoryPath);
            this.list.addEntry(dirEntry);

            // 2. Suche alle Dateien, die in DIESEM Pfad liegen
            for (Map.Entry<String, String> fileEntry : loader.Files.entrySet()) {
                String filePath = fileEntry.getKey();

                // Prüfen, ob die Datei im aktuellen Ordner liegt
                if (filePath.startsWith(directoryPath) && !filePath.equals(directoryPath)) {
                    // Den Dateinamen isolieren
                    String fileName = filePath.substring(directoryPath.length());
                    if (fileName.startsWith("/")) fileName = fileName.substring(1);

                    // Nur Dateien direkt im Ordner (keine Unterordner-Dateien hier)
                    if (!fileName.contains("/")) {
                        DocEntry fileDoc = new DocEntry(Component.literal("  " + fileName), minecraft, this.list, filePath);
                        this.list.addEntry(fileDoc);
                    }
                }
            }
        }

        for (Map.Entry<String, String> mymap : HNDoc.getInstance().getLoader().Files.entrySet()) {
            String key = mymap.getKey();
            String rawName = (key.contains("/") ? key.substring(key.lastIndexOf("/") + 1) : key);
            String displayName = rawName;//.contains(".") ? rawName.split("\\.")[0] : rawName;

            if (!key.replace(rawName, "").equals("/")) {
                continue;
            }

            if (displayName.isEmpty()) continue;

            DocEntry entry = new DocEntry(Component.literal(displayName), minecraft, this.list, key);
            this.list.addEntry(entry);
        }

        //currentText = new TextScreen(400, this.height - 40, (this.width / 2) - 200, 20, this, Component.literal("text"), "test");

        //currentText.active = false;
        //currentText.visible = false;

        this.addRenderableWidget(this.list);
        //this.addRenderableWidget(this.currentText);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        int centerX = this.width / 2;
        //graphics.fill(centerX - 110, 35, centerX + 110, this.height - 40, 0x80000000);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = GUIwidth / 2;
        graphics.fill(centerX - (GUIwidth / 2) + 10, 10, GUIwidth, this.height - 10, 0x80000000);

    }

    public <T extends GuiEventListener & Renderable & NarratableEntry> T render(T widget) {
        return this.addRenderableWidget(widget);
    }

}
