package org.emil.hnrpmc.hnessentials.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.emil.hnrpmc.hnessentials.CosmeticSlot;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CosmeticType;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CustomCosmetic;
import org.emil.hnrpmc.hnessentials.network.AdminUpdateDataPayload;
import org.emil.hnrpmc.hnessentials.network.CosmeticRegistry;
import org.emil.hnrpmc.hnessentials.network.requestPlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.emil.hnrpmc.hnessentials.menu.AdminPlayerDataScreen.listEffects;

public class VIPPlayerDataScreen extends Screen {
    private final HNPlayerData data;
    private final UUID targetUUID;
    private final String targetName;

    public VIPPlayerDataScreen(UUID targetUUID, String targetName, HNPlayerData data) {
        super(Component.literal("Cosmetics: " + targetName));
        this.targetUUID = data.getPlayerUUID();
        this.targetName = targetName;
        this.data = data;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int buttonWidth = 200;

        this.addRenderableWidget(Button.builder(
                        Component.literal("Hats"),
                        button -> {
                            this.minecraft.setScreen(new VIPCosmeticSelect(targetUUID, targetName, data, CosmeticType.HAT, this));
                        })
                .bounds(centerX - 100, startY + 0, buttonWidth, 20)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.literal("Skin Effects"),
                        button -> {
                            this.minecraft.setScreen(new VIPCosmeticSelect(targetUUID, targetName, data, null, this));
                        })
                .bounds(centerX - 100, startY + 65, buttonWidth, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("§cSchließen"), b -> this.onClose())
                .bounds(centerX - 100, this.height - 40, buttonWidth, 20)
                .build());
    }

    private void sendUpdate(String option, String value) {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
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
}