package org.emil.hnrpmc.hnessentials.cosmetics;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.codec.binary.Base64;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Cape;
import org.emil.hnrpmc.hnessentials.cosmetics.api.Model;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.CosmeticModelLoader;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.textures.AnimatedTexture;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.textures.Base64Texture;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CosmeticaSkinManager {
    private static Map<ResourceLocation, AbstractTexture> textures = new HashMap<>();
    /**
     * Stores capes that have been both loaded and uploaded.
     */
    private static Set<ResourceLocation> uploaded = new HashSet<>();
    private static final MessageDigest SHA1;

    static {
        try {
            SHA1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 Hashing not supported by the current Java Configuration.", e);
        }
    }

    public static void clearCaches() {
        textures = new HashMap<>();
        uploaded = new HashSet<>();
    }

    public static boolean isUploaded(ResourceLocation id) {
        synchronized(uploaded) {
            return uploaded.contains(id);
        }
    }

    public static ResourceLocation testId(String id) {
        return ResourceLocation.tryBuild("hnrpmc", "test/" + id);
    }

    public static ResourceLocation textureId(String type, String id) {
        return ResourceLocation.tryBuild("hnrpmc", type + "/" + pathify(id));
    }

    public static void setTestUploaded(String testId) {
        synchronized (uploaded) {
            uploaded.add(testId(testId));
        }
    }

    public static String pathify(String id) {
        StringBuilder result = new StringBuilder();

        for (char c : id.toCharArray()) {
            if (c == '+') {
                result.append(".");
            }
            else if (c == '=') {
                result.append("__");
            }
            else if (Character.isUpperCase(c)) {
                result.append("_").append(Character.toLowerCase(c));
            }
            else {
                result.append(c);
            }
        }

        return result.toString();
    }

    public static ResourceLocation processIcon(String base64Texture) {
        return saveTexture(textureId("icon", Base64.encodeBase64String(SHA1.digest(base64Texture.getBytes()))), base64Texture, 50 * 2);
    }

    public static ResourceLocation processModel(Model model) {
        return saveTexture(textureId(model.getType().getUrlString(), model.getId()), model.getTexture(), 50 * ((model.flags() >> 4) & 0x1F));
    }

    public static ResourceLocation processCape(Cape cloak) {
        return saveTexture(textureId("cape", cloak.getId()), cloak.getImage(), cloak.getFrameDelay());
    }

    public static ResourceLocation processSkin(@Nullable String base64Skin, UUID uuid) {
        if (base64Skin == null) {
            return DefaultPlayerSkin.get(uuid).texture();
        }

        return saveTexture(ResourceLocation.tryBuild("hnrpmc", "skin/" + uuid.toString().toLowerCase(Locale.ROOT)), base64Skin, 0);
    }

    private static ResourceLocation saveTexture(ResourceLocation id, String texture, int mspf) {
        if (!textures.containsKey(id)) {
            try {
                String type = id.getPath().split("\\/")[0];
                AnimatedTexture tex = createTexture(type, id, texture, mspf);

                if (RenderSystem.isOnRenderThreadOrInit()) {
                    Minecraft.getInstance().getTextureManager().register(id, tex);
                    synchronized(uploaded) { uploaded.add(id); }
                }
                else {
                    RenderSystem.recordRenderCall(() -> {
                        Minecraft.getInstance().getTextureManager().register(id, tex);
                        synchronized (uploaded) {
                            uploaded.add(id);
                        }
                    });
                }


                textures.put(id, tex);
            } catch (IOException e) {
                HNessentials.LOGGER.error("Error loading texture", e);
                return null;
            }
        }

        return id;
    }

    /**
     * Creates a potentially animated texture based on the type, raw texture data, id, and milliseconds per frame. Does
     * not register the texture.
     * @param type the type of texture to create. This handles how the aspect ratio is handled in loading the texture.
     *             By default, the texture will be treated as a square tilesheet. There are two special cases:
     *             "cape" and "skin". Capes are a tilesheet of half-squares, twice as long as they are high. Skins
     *             on the other hand need special processing due to the two different skin texture formats supported by
     *             the game.
     * @param id the id of the texture.
     * @param texture the raw base64 texture data, including the 22-character header.
     * @param mspf the number of milliseconds each frame should last for. This is ignored if the texture is not a
     *                tilesheet of multiple frames.
     * @return the created texture.
     * @throws IOException if there is an error reading the texture.
     */
    private static AnimatedTexture createTexture(String type, ResourceLocation id, String texture, int mspf) throws IOException {
        // Sicherheitscheck: Ist die Textur vorhanden?
        if (texture == null || texture.isEmpty()) {
            throw new IOException("Texture string for " + id + " is empty or null!");
        }

        // Nur abschneiden, wenn der typische Data-URL Header vorhanden ist
        if (texture.startsWith("data:image/png;base64,")) {
            texture = texture.substring(22);
        } else if (texture.length() < 22) {
            // Falls der String keine 22 Zeichen hat, würde substring(22) abstürzen
            Cosmetica.LOGGER.error("Texture for {} is too short to be a valid Base64 string!", id);
            return null; // Oder eine Standard-leere Textur zurückgeben
        }

        if ("cape".equals(type)) {
            return Base64Texture.cape(id, texture, mspf);
        } else if ("skin".equals(type)) {
            return Base64Texture.skin(id, texture);
        } else {
            return Base64Texture.square(id, texture, mspf);
        }
    }
}
