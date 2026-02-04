package org.emil.hnrpmc.hnessentials.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

// Eine minimale Implementierung eines AbstractClientPlayer für Rendering-Zwecke
public class DummyPlayer extends AbstractClientPlayer {

    public DummyPlayer(ClientLevel level, Vec3 pos, float yRot, float xRot) {
        // Dummy GameProfile mit festem UUID und Namen für Steve-Skin
        super(level, new GameProfile(UUID.fromString("8e12ad12-a740-4246-86d1-477025816f12"), "SteveGhost"));
        this.setPos(pos);
        this.setYRot(yRot);
        this.setXRot(xRot);
        this.yHeadRot = yRot; // Kopfrotation anpassen
        this.yBodyRot = yRot; // Körperrotation anpassen
        this.yRotO = yRot;
        this.xRotO = xRot;
        this.yHeadRotO = yRot;
        this.yBodyRotO = yRot;
        this.setInvisible(false); // Sichtbar machen
        this.setNoGravity(true); // Schwebend
        this.setDeltaMovement(Vec3.ZERO);
        this.setPose(Pose.STANDING); // Standard-Pose
    }

    // Nötige Overrides, die für unser Rendering nicht relevant sind
    @Override
    public boolean isSpectator() { return false; }

    @Override
    public boolean isCreative() { return false; }

    @Override
    public boolean isLocalPlayer() { return false; } // Nicht der lokale Spieler

    @Override
    public boolean isCrouching() { return false; }

    // Für GameProfileHelper.get = PlayerSkin.STEVE
    public String getModelName() {
        return "default"; // "default" oder "slim" für Alex
    }
}