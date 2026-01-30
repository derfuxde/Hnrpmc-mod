package org.emil.hnrpmc.hnessentials.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Model;
import org.emil.hnrpmc.hnessentials.cosmetics.model.*;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.FakePlayer;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.MenuRenderLayer;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.Playerish;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.TextComponents;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.collections.HashMapBackedLazyMap;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.collections.LazyMap;

import java.util.OptionalInt;

public class ShoulderBuddies<T extends AbstractClientPlayer> extends CustomLayer<T, PlayerModel<T>> implements MenuRenderLayer {
    public ShoulderBuddies(RenderLayerParent<T, PlayerModel<T>> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.builtInModels = new HashMapBackedLazyMap<>();

        this.builtInModels.put("-sheep", () -> new NZSheepBuiltinModel(entityModelSet));
        this.builtInModels.put("-persiancat", () -> new PersianCatBuiltinModel(entityModelSet));
    }

    private final LazyMap<String, BuiltInModel> builtInModels;

    @Override
    public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLight, T player, float f, float g, float pitch, float j, float k, float l) {
        if (player.isInvisible()) return;

        boolean canOverridePlayerCosmetics = this.canOverridePlayerCosmetics(player);

        PlayerData playerData = PlayerData.get(player);

        BakableModel left = canOverridePlayerCosmetics ? LEFT_OVERRIDDEN.get(playerData::leftShoulderBuddy) : playerData.leftShoulderBuddy();
        BakableModel right = canOverridePlayerCosmetics ? RIGHT_OVERRIDDEN.get(playerData::rightShoulderBuddy) : playerData.rightShoulderBuddy();

        if (left != null && ((left.extraInfo() & Model.SHOW_SHOULDER_BUDDY_WITH_PARROT) != 0 || player.getShoulderEntityLeft().isEmpty())) render(left, stack, multiBufferSource, packedLight, (Playerish) player, true);
        if (right != null && ((right.extraInfo() & Model.SHOW_SHOULDER_BUDDY_WITH_PARROT) != 0 || player.getShoulderEntityRight().isEmpty())) render(right, stack, multiBufferSource, packedLight, (Playerish) player, false);
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, FakePlayer player, float o, float n, float delta, float bob, float yRotDiff, float xRot) {
        BakableModel left = LEFT_OVERRIDDEN.get(() -> player.getData().leftShoulderBuddy());
        BakableModel right = RIGHT_OVERRIDDEN.get(() -> player.getData().rightShoulderBuddy());

        if (left != null) render(left, stack, bufferSource, packedLight, player, true);
        if (right != null) render(right, stack, bufferSource, packedLight, player, false);
    }

    public void render(BakableModel modelData, PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, Playerish player, boolean left) {
        stack.pushPose();

        if (this.builtInModels.containsKey(modelData.id())) { // builtin live sheep
            this.builtInModels.get(modelData.id()).render(stack, multiBufferSource, player, left, packedLightProbably);
//			this.builtInModels.get("-persiancat").render(stack, multiBufferSource, player, left, packedLightProbably);
        }
        else {
            boolean staticPosition = staticOverride.orElse(modelData.extraInfo() & Model.LOCK_SHOULDER_BUDDY_ORIENTATION) == Model.LOCK_SHOULDER_BUDDY_ORIENTATION;

            if (staticPosition) {
                stack.translate(left ? 0.375 : -0.375, -0.2, player.isSneaking() ? -0.16 : 0);
                doCoolRenderThings(modelData, this.getParentModel().body, stack, multiBufferSource, packedLightProbably, 0, 0.044f, 0, !left && (modelData.extraInfo() & Model.DONT_MIRROR_SHOULDER_BUDDY) == 0);
            } else {
                ModelPart modelPart = left ? this.getParentModel().leftArm : this.getParentModel().rightArm;
                doCoolRenderThings(modelData, modelPart, stack, multiBufferSource, packedLightProbably, 0, 0.37f, 0, !left && (modelData.extraInfo() & Model.DONT_MIRROR_SHOULDER_BUDDY) == 0);
            }
        }

        stack.popPose();
    }

    public static final CosmeticStack<BakableModel> LEFT_OVERRIDDEN = new CosmeticStack();
    public static final CosmeticStack<BakableModel> RIGHT_OVERRIDDEN = new CosmeticStack();
    public static OptionalInt staticOverride = OptionalInt.empty();

    static {
        BuiltInModel.NOTICES.put("-sheep", TextComponents.translatable("cosmetica.rsenotice.kiwi"));
        BuiltInModel.NOTICES.put("-persiancat", TextComponents.translatable("cosmetica.rsenotice.iranian"));
    }
}
