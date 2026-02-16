package org.emil.hnrpmc.hnessentials.menu;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CosmeticType;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CustomCosmetic;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Model;
import org.emil.hnrpmc.hnessentials.cosmetics.api.SimpleCosmetic;
import org.emil.hnrpmc.hnessentials.network.AdminUpdateDataPayload;
import org.emil.hnrpmc.hnessentials.network.CosmeticRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class VIPCosmeticSelect extends Screen {
    private final HNPlayerData data;
    private final UUID targetUUID;
    private final String targetName;
    @Nullable private CosmeticType<Model> selected = null;

    private String cosmeticID;
    private final Screen before;
    private CosmeticEntryList list;
    private SkinEffEntryList Skinlist;

    public VIPCosmeticSelect(UUID targetUUID, String targetName, HNPlayerData data, @Nullable CosmeticType<Model> selected, Screen before) {
        super(Component.literal("Cosmetic Selector " + (selected != null ? selected.toString() : "Skin effect")));
        this.targetUUID = data.getPlayerUUID();
        this.targetName = targetName;
        this.data = data;
        if (selected != null) {
            this.selected = selected;
            this.cosmeticID = data.getCosmetic(selected.getAssociatedSlot());
        }else {
            cosmeticID = data.getSelectedskineffect();

        }
        this.before = before;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int buttonWidth = 200;

        if (selected != null) {
            this.list = new CosmeticEntryList(this.minecraft, this.width, this.height, 32, 60, cosmeticID, this);

            this.list.addEntry(new CosmeticEntry(Component.literal("None"), null, this.minecraft, this.list));

            for (CustomCosmetic cosmetic : CosmeticRegistry.all()) {
                if (cosmetic.getType() != selected) continue;

                this.list.addEntry(new CosmeticEntry(Component.literal(cosmetic.getName()), cosmetic, this.minecraft, this.list));
            }

            this.addRenderableWidget(this.list);
        } else {
            this.Skinlist = new SkinEffEntryList(this.minecraft, this.width, this.height, 32, 60, cosmeticID, this);

            for (String cosmetic : AdminPlayerDataScreen.listEffects) {
                this.Skinlist.addEntry(new SkinEffEntry(Component.literal(cosmetic), cosmetic, this.minecraft, this.Skinlist));
            }

            this.addRenderableWidget(this.Skinlist);
        }


        this.addRenderableWidget(Button.builder(Component.literal("§cSchließen"), b -> {
                    this.onClose();
                    if (before != null) {
                        Minecraft.getInstance().setScreen(before);
                    }
                }).bounds(centerX - 100, this.height - 40, buttonWidth, 20)
                .build());
    }

    private void sendUpdate(String option, String value) {
        PacketDistributor.sendToServer(
                new AdminUpdateDataPayload(targetUUID, option, value)
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        int centerX = this.width / 2;
        graphics.fill(centerX - 110, 35, centerX + 110, this.height - 40, 0x80000000);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    public void setCosmeticID(String cosmeticID) {
        this.cosmeticID = cosmeticID;
        if (selected == null) {
            data.setSelectedskineffect(cosmeticID);
            sendUpdate("skineffect", cosmeticID);
            return;
        }
        data.setCosmetic(selected.getAssociatedSlot(), cosmeticID);
        sendUpdate("cosmetic", cosmeticID);
    }
}