package org.emil.hnrpmc.hnessentials;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class Tpa {
    private final ServerPlayer requester;
    private final ServerPlayer receiver;
    private final boolean here;
    private final TpaUsers tpaUsers;
    private final Vec3 sendPos;
    private final ServerLevel sendDimension;

    private final String tpaID;

    public Tpa(ServerPlayer requester, ServerPlayer receiver, boolean here) {
        this.requester = requester;
        this.receiver = receiver;
        this.here = here;
        this.sendPos = requester.getPosition(0);

        this.sendDimension = requester.serverLevel();

        this.tpaUsers = new TpaUsers(requester.getUUID(), receiver.getUUID());

        StringBuilder randomtpaid = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            Random random = new Random();
            char randomChar = (char) (random.nextInt(26) + 'A');

            randomtpaid.append(randomChar);
        }

        this.tpaID = randomtpaid.toString();
    }

    public ServerPlayer getRequester() {
        return this.requester;
    }

    public ServerPlayer getReceiver() {
        return this.receiver;
    }

    public String getTpaID() {
        return this.tpaID;
    }

    public boolean isHere() {
        return this.here;
    }

    public TpaUsers getTpaUsers() {
        return this.tpaUsers;
    }

    public Vec3 getSendPos() {
        return this.sendPos;
    }

    public ServerLevel getSendDimension() {
        return this.sendDimension;
    }
}
