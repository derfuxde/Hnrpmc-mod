package org.emil.hnrpmc.hnessentials.mixin;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ClientHooks;
import org.emil.hnrpmc.hnessentials.cosmetics.mixin.LivingEntityRendererAccessor;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class SavedPlayerModel {


    public float elytraRotX;
    public float elytraRotY;
    public float elytraRotZ;
    public final ClientLevel clientLevel;
    public int takeXpDelay;
    public double xCloakO;
    public double yCloakO;
    public double zCloakO;
    public double xCloak;
    public double yCloak;
    public double zCloak;

    public PlayerModel<AbstractClientPlayer> model = null;

    public SavedPose pose;

    List<RenderLayer<?, ?>> layers;

    public SavedPlayerModel(ClientLevel clientLevel, AbstractClientPlayer abstractClientPlayer) {
        EntityRenderer<? super AbstractClientPlayer> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(abstractClientPlayer);
        if (renderer instanceof PlayerRenderer playerRenderer) {
            model = playerRenderer.getModel();

            layers = ((LivingEntityRendererAccessor) playerRenderer).getLayers();

            ElytraLayer<LivingEntity, ElytraModel<LivingEntity>> ELG = null;
            for (RenderLayer<?, ?> layer : layers) {
                if (layer instanceof ElytraLayer<?, ?> EL) {
                    ELG = (ElytraLayer<LivingEntity, ElytraModel<LivingEntity>>) EL;
                }
            }

            pose = new SavedPose(model, ELG, abstractClientPlayer.isFallFlying(), abstractClientPlayer.isVisuallySwimming());
        }
        this.clientLevel = clientLevel;
    }

}
