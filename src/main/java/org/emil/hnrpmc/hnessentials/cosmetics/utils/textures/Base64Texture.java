package org.emil.hnrpmc.hnessentials.cosmetics.utils.textures;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.cosmetics.mixin.textures.NativeImageAccessorMixin;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Texture extends AnimatedTexture {
    private Base64Texture(ResourceLocation path, String base64, NativeImage initialImage, int aspectRatio) throws IOException {
        super(aspectRatio);
        this.base64 = base64;
        this.path = path;

        this.loadImage(initialImage);
    }

    private final ResourceLocation path;
    private final String base64;

    @Override
    public void load(ResourceManager resourceManager) {
        // Prüfe einfach, ob das Bild-Objekt da ist
        if (this.image == null) {
            if (RenderSystem.isOnRenderThreadOrInit()) {
                this.reload();
            } else {
                RenderSystem.recordRenderCall(this::reload);
            }
            return;
        }

        this.upload();
    }

    private void reload() {
        try {
            this.loadImage(loadBase64(this.base64)); // load the image
            this.upload();
        } catch (IOException e) {
            HNessentials.LOGGER.error("Error re-uploading Base64 Texture", e);
        }
    }

    private void loadImage(NativeImage image) {
        this.image = image;

        if (this.isAnimatable()) {
            this.setupAnimations();
        }
        else {
            this.setupStatic();
        }
    }

    private static NativeImage loadBase64(String base64) throws IOException {
        // fromBase64 was removed in 1.19.4
//        if(base64.length() < 1000) { //TODO: Tweak this number
//            return NativeImage.read(base64);
//        } else {
        //For large images, NativeImage.fromBase64 does not work because it tries to allocate it on the stack and fails
        byte[] bs = Base64.getDecoder().decode(base64.replace("\n", "").getBytes(StandardCharsets.UTF_8));
        ByteBuffer buffer = MemoryUtil.memAlloc(bs.length);
        buffer.put(bs);
        buffer.rewind();
        NativeImage image = NativeImage.read(buffer);
        MemoryUtil.memFree(buffer);
        return image;
//        }
    }

    public static Base64Texture square(ResourceLocation path, String base64, int frameDelayMs) throws IOException {
        NativeImage image = loadBase64(base64);

        if (image.getHeight() > image.getWidth()) {
            return new TickingCape(path, base64, image, frameDelayMs, 1);
        }
        else {
            return new Base64Texture(path, base64, image, 0);
        }
    }

    public static Base64Texture cape(ResourceLocation path, String base64, int frameDelayMs) throws IOException {
        NativeImage image = loadBase64(base64);

        if (image.getHeight() >= image.getWidth()) {
            return new TickingCape(path, base64, image, frameDelayMs, 2);
        }
        else {
            return new Base64Texture(path, base64, image, 0);
        }
    }

    public static Base64Texture skin(ResourceLocation path, String base64) throws IOException {
        return new Base64Texture(path, base64, loadBase64(base64), 0);
    }

    private static class TickingCape extends Base64Texture implements Tickable {
        private TickingCape(ResourceLocation path, String base64, NativeImage initialImage, int frameDelayMs, int aspectRatio) throws IOException {
            super(path, base64, initialImage, aspectRatio);
            this.frameCounterTicks = Math.max(1, frameDelayMs / 50);
        }

        @Override
        public void tick() {
            this.doTick();
        }
    }
}
