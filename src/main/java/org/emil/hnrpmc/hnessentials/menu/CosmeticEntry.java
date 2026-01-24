package org.emil.hnrpmc.hnessentials.menu;

import com.hypherionmc.sdlink.shaded.okhttp3.internal.connection.RouteSelector;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.emil.hnrpmc.hnessentials.Cosmetic;
import org.emil.hnrpmc.hnessentials.cosmetics.CapeData;
import org.emil.hnrpmc.hnessentials.cosmetics.PlayerData;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CosmeticType;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CustomCosmetic;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Model;
import org.emil.hnrpmc.hnessentials.cosmetics.impl.CosmeticFetcher;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;
import org.emil.hnrpmc.hnessentials.cosmetics.model.Models;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.FakePlayer;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.FakePlayerRenderer;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.LinearAlgebra;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CosmeticEntry extends ObjectSelectionList.Entry<CosmeticEntry> {
    private final Component text;
    private final Minecraft minecraft;
    private final CustomCosmetic cosmetic;

    public CosmeticEntry(Component text, CustomCosmetic cosmetic, Minecraft minecraft, CosmeticEntryList parent) {
        this.text = text;
        this.minecraft = minecraft;
        this.cosmetic = cosmetic;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
        guiGraphics.drawString(this.minecraft.font, this.text, left + 45, top + 10, 0xFFFFFFFF);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        poseStack.translate(left + 20, top + 20, 100);
        poseStack.scale(15.0F, -15.0F, 15.0F);

        poseStack.mulPose(Axis.XP.rotationDegrees(30));
        poseStack.mulPose(Axis.YP.rotationDegrees(45));

        poseStack.popPose();

        Player player = minecraft.player;

        if (player == null) return;

        List<BakableModel> hatbakableModels = new ArrayList<>();
        Model hatmodel = CosmeticFetcher.getModel((CosmeticType<Model>) cosmetic.getType(), cosmetic.getId());
        if (hatmodel != null) {
            BakableModel hatbm = Models.createBakableModel(hatmodel);
            if (hatbm != null) {
                hatbakableModels.add(hatbm);
            }
        }

        PlayerSkin skin = Minecraft.getInstance().getSkinManager().getInsecureSkin(player.getGameProfile());

        ResourceLocation texture = skin.texture();

        boolean isSlim = skin.model() == PlayerSkin.Model.SLIM;

        //CapeData CD = new CapeData(skin.capeTexture(), "mycape", "mycape", true, );
        PlayerData playerData = new PlayerData("", false, null, true, "", "", hatbakableModels, CapeData.NO_CAPE, null, null, null, texture, isSlim);

        guiGraphics.enableScissor(left, top, left + 10 + width, top + height);
        FakePlayer newFaker = new FakePlayer(this.minecraft, player.getUUID(), player.getDisplayName().getString(), playerData);
        newFaker.renderNametag = false;
        renderFakePlayerInMenu(left + 20, top + 55, 10.0f, (float) left - mouseX, (float)(top - 90) - mouseY, newFaker);
        //guiGraphics.blit(this.image, left, top, 0, 0, 20, 20, 20, 20);
        guiGraphics.disableScissor();
    }

    public static void renderFakePlayerInMenu(int left, int top, float extraScale, float lookX, float lookY, FakePlayer fakePlayer) {
        float h = (float)Math.atan(lookX / 40.0F);
        float l = (float)Math.atan(lookY / 40.0F);
        Matrix4fStack stack = RenderSystem.getModelViewStack();

        stack.pushMatrix();
        stack.translate(left, top, 1050.0f);
        stack.scale(2.0F, 2.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();

        // view
        PoseStack viewStack = new PoseStack();
        viewStack.translate(0.0D, 0.0D, 1000.0D);
        viewStack.scale(extraScale, extraScale, extraScale);
        Quaternionf zRotation = LinearAlgebra.quaternionDegrees(LinearAlgebra.ZP, 180.0F);
        Quaternionf xRotation = LinearAlgebra.quaternionDegrees(LinearAlgebra.XP, l * 20.0F);
        zRotation.mul(xRotation);
        viewStack.mulPose(zRotation);

        float rotationBody = 180.0F + h * 20.0F;
        float rotationMain = 180.0F + h * 40.0F;
        fakePlayer.yRotBody += rotationBody;
        fakePlayer.yRot += rotationMain;
        fakePlayer.xRot = -l * 20.0F;
        fakePlayer.yRotHead = fakePlayer.getYRot(0);
        Lighting.setupForEntityInInventory();

        xRotation.conjugate(); // originally conj() in mojang's maths library
        FakePlayerRenderer.cameraOrientation = xRotation;
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        RenderSystem.runAsFancy(() -> {
            if (fakePlayer.verifyModel(Minecraft.getInstance())) {
                FakePlayerRenderer.render(viewStack, fakePlayer, bufferSource, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, 15728880);
            }
        });
        bufferSource.endBatch();

        fakePlayer.yRotBody -= rotationBody;
        fakePlayer.yRot -= rotationMain;

        stack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    @Override
    public Component getNarration() {
        return text;
    }
}
