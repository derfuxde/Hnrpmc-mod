package org.emil.hnrpmc.hnessentials;

import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class Home {
    private Vec3 coords;
    private UUID ownerUUID;
    private String homename;
    private String world_name;

    public Home(UUID ownerUUID, Vec3 coords, String homename, String world_name) {
        this.ownerUUID = ownerUUID;
        this.coords = coords;
        this.homename = homename;
        this.world_name = world_name;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public Vec3 getCoords() {
        return coords;
    }

    public String getHomename() {
        return homename;
    }

    public String getWorld_name() {
        return world_name;
    }
}
