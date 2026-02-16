package org.emil.hnrpmc.doc.menu.components;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.emil.hnrpmc.doc.HNDoc;
import org.emil.hnrpmc.hnessentials.cosmetics.CapeData;
import org.emil.hnrpmc.hnessentials.cosmetics.PlayerData;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CustomCosmetic;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Model;
import org.emil.hnrpmc.hnessentials.cosmetics.impl.CosmeticFetcher;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;
import org.emil.hnrpmc.hnessentials.cosmetics.model.Models;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.FakePlayer;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.FakePlayerRenderer;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.LinearAlgebra;
import org.emil.hnrpmc.hnessentials.menu.CosmeticEntry;
import org.emil.hnrpmc.hnessentials.menu.CosmeticEntryList;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.*;

public class DocEntry extends ObjectSelectionList.Entry<DocEntry> {
    private final Component text;
    private final Minecraft minecraft;
    private final DocList parent;
    private final String path;

    public DocEntry(Component text, Minecraft minecraft, DocList parent, String path) {
        this.text = text;
        this.minecraft = minecraft;
        this.parent = parent;
        this.path = path;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
        guiGraphics.drawString(this.minecraft.font, this.text, left + 65, top + 5, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        parent.setSelected(this);

        String displayName = this.text.getString();

        for (Map.Entry<String, String> fileEntry : HNDoc.getInstance().getLoader().Files.entrySet()) {
            String fullPath = fileEntry.getKey();
            if (fullPath.replace("src/Doc", "").equals(path)) {
                try {
                    //String fileContent = fileEntry.getValue();

                    //Gson gson = HNDoc.getInstance().getGson();
                    //Type listType = new TypeToken<Map<String, ?>>(){}.getType();
                    //Map<String, ?> item = gson.fromJson(fileContent, listType);

                    String itemPath = fullPath.replace("src/Doc", "");


                    String jsonResponse = HNDoc.getInstance().getLoader().Files.get(itemPath);


                    if (parent.getParened().currentText != null) {
                        parent.getParened().currentText.active = false;
                        parent.getParened().currentText.visible = false;
                    }

                    TextScreen ts = new TextScreen(parent.getParened().GUIwidth - 175, parent.getHeight() - 35, (parent.getWidth() / 2) + 85, 35, parent.getParened(), Component.literal(displayName), jsonResponse);

                    parent.getParened().render(ts);
                    parent.getParened().currentText = ts;
                } catch (Exception e) {
                    System.out.println("error " + e);
                    return false;
                }


                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public Component getNarration() {
        return text;
    }
}
