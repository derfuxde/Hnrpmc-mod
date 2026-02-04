package org.emil.hnrpmc.hnessentials.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.FakePlayer;

public interface GohstMenuRenderLayer {
    void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, PlayerRendereType player, float o, float n, float delta, float bob, float yRotDiff, float xRot);
    void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, PlayerRendereType player, float o, float n, float delta, float bob, float yRotDiff, float xRot, float alpha);
}
