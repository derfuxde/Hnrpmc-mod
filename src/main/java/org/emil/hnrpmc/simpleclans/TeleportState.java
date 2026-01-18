package org.emil.hnrpmc.simpleclans;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public class TeleportState {

    private final UUID playerUUID;
    private final ServerLevel originLevel;
    private final Vec3 originPos;
    private final ServerLevel destLevel;
    private final Vec3 destPos;

    private int counter;
    private final String clanName;
    private boolean processing;

    // Konstruktor nutzt ServerPlayer für Online-Daten und Positionen
    public TeleportState(ServerPlayer player, ServerLevel destLevel, Vec3 destPos, String clanName, int counter) {
        this.playerUUID = player.getUUID();
        this.originLevel = player.serverLevel();
        this.originPos = player.position();
        this.destLevel = destLevel;
        this.destPos = destPos;
        this.clanName = clanName;
        this.counter = counter;
    }

    // Entspricht Bukkit origin.getLocation()
    public Vec3 getOriginPos() {
        return this.originPos;
    }

    public ServerLevel getOriginLevel() {
        return this.originLevel;
    }

    public boolean isTeleportTime() {
        if (this.counter > 1) {
            this.counter--;
            return false;
        }
        return true;
    }

    /**
     * Holt den aktuellen Online-Spieler vom Server via UUID
     */
    public @Nullable ServerPlayer getPlayer(net.minecraft.server.MinecraftServer server) {
        return server.getPlayerList().getPlayer(playerUUID);
    }

    public int getCounter() {
        return this.counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getClanName() {
        return this.clanName;
    }

    public Vec3 getDestinationPos() {
        return this.destPos;
    }

    public ServerLevel getDestinationLevel() {
        return this.destLevel;
    }

    public boolean isProcessing() {
        return this.processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }
}