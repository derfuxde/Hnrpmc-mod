package org.emil.hnrpmc.hnessentials.cosmetics;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.lang.ref.WeakReference;

public final class CosmeticaCapes {
    public CosmeticaCapes(Player player) {
        this.player = player;
    }

    private final Player player;

    // avoid allocating memory every time getSkin is called!
    @Unique
    private WeakReference<@Nullable PlayerSkin> cachedVanillaSkin = new WeakReference<>(null);

    @Nullable
    public PlayerSkin addCosmeticaCapes(GameProfile profile, PlayerSkin vanillaSkin) {
        if (!Cosmetica.isProbablyNPC(profile.getId())) { // ignore npcs
            if (!PlayerData.has(profile.getId()))
                return null;

            CapeData cape = PlayerData.get(player).cape();
            ResourceLocation location = cape.getImage(); // get the location if cached
            if (location != null && !CosmeticaSkinManager.isUploaded(location))
                location = null; // only actually get it if it's been uploaded

            if (location != null) {
                @Nullable PlayerSkin cached = cachedVanillaSkin.get();

                if (cached != vanillaSkin) {
                    cachedVanillaSkin = new WeakReference<>(cached);
                    cape.clearSkinCache();
                }

                return cape.getSkin(vanillaSkin);
            }
        }

        return null;
    }
}