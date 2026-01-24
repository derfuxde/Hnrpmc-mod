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

public class AdminPlayerDataScreen extends Screen {
    private final HNPlayerData data;
    private final UUID targetUUID;
    private final String targetName;

    public AdminPlayerDataScreen(UUID targetUUID, String targetName, HNPlayerData data) {
        super(Component.literal("Admin Editor: " + targetName));
        this.targetUUID = data.getPlayerUUID();
        this.targetName = targetName;
        this.data = data;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int buttonWidth = 200;

        // 1. GODMODE BUTTON
        this.addRenderableWidget(Button.builder(
                        Component.literal("Godmode: " + (data.isGodMode() ? "§aAN" : "§cAUS")),
                        button -> {
                            boolean newState = !data.isGodMode();
                            data.setGodMode(newState);
                            sendUpdate("godmode", String.valueOf(newState));
                            button.setMessage(Component.literal("Godmode: " + (newState ? "§aAN" : "§cAUS")));
                        })
                .bounds(centerX - 100, startY, buttonWidth, 20)
                .build());

        // 2. MUTE BUTTON
        this.addRenderableWidget(Button.builder(
                        Component.literal("Mute: " + (data.isMuted() ? "§aJA" : "§cNEIN")),
                        button -> {
                            boolean newState = !data.isMuted();
                            data.setMuted(newState);
                            sendUpdate("muted", String.valueOf(newState));
                            button.setMessage(Component.literal("Mute: " + (newState ? "§aJA" : "§cNEIN")));
                        })
                .bounds(centerX - 100, startY + 25, buttonWidth, 20)
                .build());

        // 3. TEXTFELD FÜR GELD (EditBox)
        // Wir erstellen ein Eingabefeld direkt im GUI
        net.minecraft.client.gui.components.EditBox moneyInput = new net.minecraft.client.gui.components.EditBox(
                this.font, centerX - 100, startY + 60, 140, 20, Component.literal("Geld")
        );
        moneyInput.setValue(String.valueOf(data.getMoney()));
        this.addRenderableWidget(moneyInput);

        // Button direkt neben dem Textfeld zum Bestätigen
        this.addRenderableWidget(Button.builder(Component.literal("Set"), button -> {
            sendUpdate("money", moneyInput.getValue());
        }).bounds(centerX + 45, startY + 60, 45, 20).build());

        // 4. TELEPORT BUTTON (Teleportiert den Admin zum Spieler)
        this.addRenderableWidget(Button.builder(Component.literal("§bZu Spieler Teleportieren"), button -> {
            // Wir schicken ein spezielles Feld an den Server
            sendUpdate("teleport_to", "true");
            this.onClose(); // Schließt das Menü
        }).bounds(centerX - 100, startY + 95, buttonWidth, 20).build());

        // 5. COSMETIC BUTTON (Beispiel für Block-Animation)
        this.addRenderableWidget(Button.builder(Component.literal("Cosmetic: " + data.getCosmetic(CosmeticSlot.HAT)), button -> {
            // Hier könnte man durch eine Liste schalten (dirt -> stone -> gold -> none)

            List<String> listHats = new ArrayList<>(CosmeticRegistry.all().stream().map(CustomCosmetic::getId).toList());
            listHats.add("none");
            String current = data.getCosmetic(CosmeticSlot.HAT);
            String currentFixed = (current == null) ? "none" : current;
            int currentIndex = listHats.indexOf(currentFixed);
            int nextIndex = (currentIndex + 1) % listHats.size();
            String nextId = listHats.get(nextIndex);
            CustomCosmetic CC = CosmeticRegistry.get(nextId);
            CosmeticSlot CS = CosmeticSlot.HAT;
            if (CC != null) CS = CC.getasCosmetic().getSlot();
            if (nextId.equals("none")) {
                data.setCosmetic(CS, null);
            } else {
                data.setCosmetic(CS, nextId);
            }

            data.setCosmetic(CS, nextId);
            sendUpdate("cosmetic", nextId);
            button.setMessage(Component.literal("Cosmetic: " + nextId));
        }).bounds(centerX - 100, startY + 120, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                        Component.literal("Hats"),
                        button -> {
                            this.minecraft.setScreen(new AdminCosmeticSelect(targetUUID, targetName, data, CosmeticType.HAT, this));
                        })
                .bounds(centerX - 100, startY + 145, buttonWidth, 20)
                .build());

        // SCHLIESSEN BUTTON (Ganz unten)
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