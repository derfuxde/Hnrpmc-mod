package org.emil.hnrpmc.hnessentials.menu;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.listeners.PlayerDataRequestPayload;
import org.emil.hnrpmc.hnessentials.network.SaveSkinPayload;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class PetMorphScreen extends Screen {
    private int selectedIndex = 0;
    public static String currentSkinName = "Default";
    private final Entity pet;
    private final HNessentials plugin;
    private final List<String> skins;

    public PetMorphScreen(Entity pet, HNessentials plugin) {
        super(Component.literal("Pet Morph"));
        this.pet = pet;
        this.plugin = plugin;
        skins = plugin.getSkins();
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft.player != null) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new PlayerDataRequestPayload(this.pet.getUUID())
            );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);

        var fontRenderer = this.font != null ? this.font : net.minecraft.client.Minecraft.getInstance().font;

        for (int i = 0; i < skins.size(); i++) {
            int yPos = 20 + (i * 20);
            int color = (i == selectedIndex) ? 0xFF00FF00 : 0xFFFFFFFF;
            String prefix = (i == selectedIndex) ? "> " : "  ";
            guiGraphics.drawString(fontRenderer, prefix + skins.get(i), 15, yPos, color);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        if (keyCode == 264) { // Pfeiltaste Runter
            selectedIndex = (selectedIndex + 1) % skins.size();
            return true;
        } else if (keyCode == 265) { // Pfeiltaste Hoch
            selectedIndex = (selectedIndex - 1 + skins.size()) % skins.size();
            return true;
        } else if (keyCode == 257) { // Enter / Bestätigen
            applySkin(skins.get(selectedIndex));
            //HNPlayerData pld = plugin.getStorageManager().getOrCreatePlayerData(player.getUUID());
            //pld.setPetSelectedTextureForPet(selectedIndex, pet.getUUID());
            //plugin.getStorageManager().setPlayerData(player.getUUID(), pld);
            //plugin.getStorageManager().save(player.getUUID());
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void updateSelectedSkin(int skinIndex) {
        this.selectedIndex = skinIndex;
        // Optional: Log zur Kontrolle
        System.out.println("Screen hat Skin-Index vom Server erhalten: " + skinIndex);
    }

    private void applySkin(String skinName) {
        if (pet instanceof Wolf wolf) {
            // Registry für Wolf-Varianten holen
            //        this.wildTexture = p_332712_;
            //        this.wildTextureFull = fullTextureId(p_332712_);
            //        this.tameTexture = p_332714_;
            //        this.tameTextureFull = fullTextureId(p_332714_);
            //        this.angryTexture = p_332788_;
            //        this.angryTextureFull = fullTextureId(p_332788_);
            //        this.biomes = p_332717_;


            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new SaveSkinPayload(pet.getUUID().toString(), selectedIndex)
            );

            var registry = wolf.level().registryAccess().lookupOrThrow(Registries.WOLF_VARIANT);

            ResourceKey<WolfVariant> key = WolfVariants.PALE;

            if (skinName.equals("Gold")) {
                key = ResourceKey.create(Registries.WOLF_VARIANT,
                        ResourceLocation.fromNamespaceAndPath("hnrpmc", "gold"));
                ResourceLocation normal = ResourceLocation.fromNamespaceAndPath("hnrpmc", "entity/wolf/gold_wolf");
                ResourceLocation angry = ResourceLocation.fromNamespaceAndPath("hnrpmc", "entity/wolf/angry_gold_wolf");

                var biomeRegistry = wolf.level().registryAccess().lookupOrThrow(Registries.BIOME);
                HolderSet<Biome> biomeSet = biomeRegistry.getOrThrow(BiomeTags.IS_FOREST);

                WolfVariant customVariant = new WolfVariant(normal, normal, angry, null);

                wolf.setVariant(Holder.direct(customVariant));
            } else if (skinName.equals("Shadow")) {
                key = WolfVariants.BLACK;
            } else if (skinName.equals("Rainbow")) {

            }

            registry.get(key).ifPresent(wolf::setVariant);
        }
    }

    public void drawTintedImage(Graphics2D g2d, BufferedImage img, Color tintColor, int x, int y) {
        // 1. Das Originalbild zeichnen
        g2d.drawImage(img, x, y, null);

        // 2. Die Farbe mit Transparenz setzen (z.B. 128 für 50% Deckkraft)
        Color transparentColor = new Color(tintColor.getRed(), tintColor.getGreen(), tintColor.getBlue(), 128);
        g2d.setColor(transparentColor);

        // 3. Ein Rechteck in der Größe des Bildes darüberlegen
        g2d.fillRect(x, y, img.getWidth(), img.getHeight());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}