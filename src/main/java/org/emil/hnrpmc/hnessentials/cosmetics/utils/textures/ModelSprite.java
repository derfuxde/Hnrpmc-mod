package org.emil.hnrpmc.hnessentials.cosmetics.utils.textures;

import com.google.common.collect.ImmutableList;
import com.hypherionmc.craterlib.common.NeoForgeLoaderHelper;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class ModelSprite extends TextureAtlasSprite {
    public ModelSprite(ResourceLocation location, AnimatedTexture texture) {
        this(location, texture, texture.image.getWidth(), texture.getFrameHeight());
    }

    private ModelSprite(ResourceLocation location, AnimatedTexture texture, int width, int height) {
        // textureAtlas, info, mipLevels, uScale (atlasTextureWidth), vScale (atlasTextureHeight), width, height, image
        super(null,
                new ModelSpriteContents(location, new FrameSize(width, height), texture),
                width, height,
                0, 0
        );

        this.animatedTexture = texture;
        this.location = location;
    }

    private final AnimatedTexture animatedTexture;
    private final ResourceLocation location;

    @Override
    public String toString() {
        return "ModelSprite{" +
                "animatedTexture=" + animatedTexture +
                ", resourceLocation=" + this.location +
                ", u=[" + this.getU0() + "," + this.getU1() + "]" +
                ", v=[" + this.getV0() + ", " + this.getV1() + "]" +
                '}';
    }

    @Override
    public ResourceLocation atlasLocation() {
        if (NeoForgeLoaderHelper.INSTANCE.isDevEnv()) {
            throw new UnsupportedOperationException("I am a teapot. Tried to call atlasLocation() on cosmetica ModelSprite.");
        }
        else {
            // fix compat with ModelGapFix (modelfix)
            // pretend to be the block atlas
            return BLOCK_ATLAS;
        }
    }

    private static final ResourceLocation BLOCK_ATLAS = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");

    @Override
    public void uploadFirstFrame() {
        throw new UnsupportedOperationException("I am a teapot. Tried to call uploadFirstFrame() on cosmetica ModelSprite.");
    }

    public static class ModelSpriteContents extends SpriteContents {
        public ModelSpriteContents(ResourceLocation resourceLocation, FrameSize frameSize, AnimatedTexture animatedTexture) {
            super(resourceLocation,
                    frameSize,
                    animatedTexture.image,
                    new ResourceMetadata.Builder()
                            .put(AnimationMetadataSection.SERIALIZER, new AnimationMetadataSection(
                                    ImmutableList.of(new AnimationFrame(0, -1)), // DUMMY FRAME.
                                    frameSize.width(), frameSize.height(), animatedTexture.frameCounterTicks, false))
                            .build()
            );
            this.animatedTexture = animatedTexture;
        }

        private final AnimatedTexture animatedTexture;

        protected int getFrameCount() {
            return this.animatedTexture.getFrameCount();
        }

        @Override
        public IntStream getUniqueFrames() {
            return IntStream.range(0, getFrameCount());
        }

        // TODO what are these two close() functions for?
        // This one seems to close the image in vanilla, whereas ticker/close seems to close the interpolation data object
        // the latter does effectively the same thing but whatever texture is currently active in the interpolation data object
        @Override
        public void close() {
            this.animatedTexture.close();
        }

        @Nullable
        @Override
        public SpriteTicker createTicker() {
            return this.animatedTexture instanceof Tickable ? new SpriteTicker() {
                @Override
                public void tickAndUpload(int i, int j) {
                    ModelSpriteContents.this.animatedTexture.doTick();
                }

                @Override
                public void close() {
                    ModelSpriteContents.this.animatedTexture.close();
                }
            } : null;
        }
    }
}
