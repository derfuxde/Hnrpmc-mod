package org.emil.hnrpmc.hnessentials.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CosmeticType;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Model;
import org.emil.hnrpmc.hnessentials.cosmetics.impl.CosmeticFetcher;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;
import org.emil.hnrpmc.hnessentials.cosmetics.model.CosmeticStack;
import org.emil.hnrpmc.hnessentials.cosmetics.model.Models;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.FakePlayer;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.MenuRenderLayer;
import org.emil.hnrpmc.hnessentials.mixin.GohstMenuRenderLayer;
import org.emil.hnrpmc.hnessentials.mixin.PlayerRendereType;
import org.emil.hnrpmc.hnessentials.network.requestPlayerData;

import java.util.ArrayList;
import java.util.List;

public class Hats<T extends Player> extends CustomLayer<T, PlayerModel<T>> implements MenuRenderLayer, GohstMenuRenderLayer {

    public Hats(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLight, T player, float f, float g, float pitch, float j, float k, float l) {
        if (player.isInvisible()) return;
        List<BakableModel> hats = getHats(player);
        if (hats == null || hats.isEmpty()) return;

        stack.pushPose();

        for (BakableModel modelData : hats) {
            if ((modelData.extraInfo() & Model.SHOW_HAT_WITH_HELMET) == 0 && player.hasItemInSlot(EquipmentSlot.HEAD)) {

            }

            if ((modelData.extraInfo() & Model.LOCK_HAT_ORIENTATION) == 0) {
                doCoolRenderThings(modelData, this.getParentModel().getHead(), stack, multiBufferSource, packedLight, 0, 0.75f, 0);
            } else {
                doCoolRenderThings(modelData, this.getParentModel().body, stack, multiBufferSource, packedLight, 0, 0.77f, 0);
            }

            stack.scale(1.001f, 1.001f, 1.001f); // stop multiple hats conflicting
        }

        stack.popPose();
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, FakePlayer player, float o, float n, float delta, float bob, float yRotDiff, float xRot) {
        List<BakableModel> hats = OVERRIDDEN.getList(() -> player.getData().hats());

        stack.pushPose();

        for (BakableModel modelData : hats) {
            if ((modelData.extraInfo() & Model.LOCK_HAT_ORIENTATION) == 0) {
                doCoolRenderThings(modelData, this.getParentModel().getHead(), stack, bufferSource, packedLight, 0, 0.75f, 0);
            } else {
                doCoolRenderThings(modelData, this.getParentModel().body, stack, bufferSource, packedLight, 0, 0.77f, 0);
            }

            stack.scale(1.001f, 1.001f, 1.001f); // stop multiple hats conflicting
        }

        stack.popPose();
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, PlayerRendereType player, float o, float n, float delta, float bob, float yRotDiff, float xRot) {
        render(stack, bufferSource, packedLight, player, o, n, delta, bob, yRotDiff, xRot, 1.0f);
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, PlayerRendereType player, float o, float n, float delta, float bob, float yRotDiff, float xRot, float alpha) {
        List<BakableModel> hats = OVERRIDDEN.getList(() -> player.getData().hats());

        stack.pushPose();

        for (BakableModel modelData : hats) {
            if ((modelData.extraInfo() & Model.LOCK_HAT_ORIENTATION) == 0) {
                doCoolRenderThings(modelData, this.getParentModel().getHead(), stack, bufferSource, packedLight, 0, 0.75f, 0, alpha);
            } else {
                doCoolRenderThings(modelData, this.getParentModel().body, stack, bufferSource, packedLight, 0, 0.77f, 0, alpha);
            }

            stack.scale(1.001f, 1.001f, 1.001f); // stop multiple hats conflicting
        }

        stack.popPose();
    }

    public static final CosmeticStack<BakableModel> OVERRIDDEN = new CosmeticStack();

    public static List<BakableModel> getHats(Player player) {
        // 1. Hol die Map
        var map = HNessentials.getInstance().HNplayerDataMap;
        if (map == null) {
            return List.of();
        }

        var data = map.get(player.getUUID());

        if (data == null) {
            PacketDistributor.sendToServer(new requestPlayerData(player.getUUID()));
            return List.of();
        }

        List<BakableModel> bakableModels = new ArrayList<>();
        for (String hatid : data.hats()) {
            Model hatmodel = CosmeticFetcher.getModel(CosmeticType.HAT, hatid);
            if (hatmodel != null) {
                BakableModel hatbm = Models.createBakableModel(hatmodel);
                BakableModel hb2 = hatbm;
                if (hatbm != null) {
                    bakableModels.add(hatbm);
                } else {
                    continue;
                }
            }else {
                continue;
            }
        }

        // 4. Logik für Overrides (z.B. im Menu) oder normale Hats
        return canOverridePlayerCosmetics(player) ?
                OVERRIDDEN.getList(() -> bakableModels) :
                bakableModels;
    }
}