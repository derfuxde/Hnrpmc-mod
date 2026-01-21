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
import org.emil.hnrpmc.hnessentials.HNPlayerData;

import java.util.List;
import java.util.UUID;

public class AdminPlayerDataScreen extends Screen {
    private final HNPlayerData data;
    private final UUID targetUUID;
    private final String targetName;

    public AdminPlayerDataScreen(UUID targetUUID, String targetName, HNPlayerData data) {
        super(Component.literal("Admin Editor: " + targetName));
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.data = data;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;

        this.addRenderableWidget(Button.builder(
                        Component.literal("Godmode: " + (data.isGodMode() ? "§aAN" : "§cAUS")),
                        button -> {
                            boolean newState = !data.isGodMode();
                            data.setGodMode(newState);
                            // Paket an Server senden
                            sendUpdate("godmode", String.valueOf(newState));
                            this.init(minecraft, width, height); // Screen neu laden für Text-Update
                        })
                .bounds(centerX - 100, startY, 200, 20)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.literal("Mute: " + (data.isMuted() ? "§aJA" : "§cNEIN")),
                        button -> {
                            boolean newState = !data.isMuted();
                            data.setMuted(newState);
                            sendUpdate("muted", String.valueOf(newState));
                            this.init(minecraft, width, height);
                        })
                .bounds(centerX - 100, startY + 30, 200, 20)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.literal("Geld setzen (Input im Chat)"),
                        button -> {
                            minecraft.setScreen(null);
                            minecraft.player.sendSystemMessage(Component.literal("§eNutze /setplayerdata " + targetName + " money <wert>"));
                        })
                .bounds(centerX - 100, startY + 60, 200, 20)
                .build());

        // Schließen Button
        this.addRenderableWidget(Button.builder(Component.literal("Schließen"), b -> this.onClose())
                .bounds(centerX - 100, this.height - 30, 200, 20)
                .build());
    }

    private void sendUpdate(String option, String value) {
        // Hier schickst du dein existierendes Netzwerk-Paket an den Server
        // Damit der Server die PlayerData in der JSON-Datei aktualisiert
        // PacketDistributor.sendToServer(new AdminUpdateDataPayload(targetUUID, option, value));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}