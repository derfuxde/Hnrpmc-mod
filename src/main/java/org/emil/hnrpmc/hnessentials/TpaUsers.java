package org.emil.hnrpmc.hnessentials;

import java.util.UUID;

public class TpaUsers {
    private final UUID requesterUUID;
    private final UUID receiverUUID;

    public TpaUsers(UUID requesterUUID, UUID receiverUUID) {
        this.requesterUUID = requesterUUID;
        this.receiverUUID = receiverUUID;
    }

    public UUID getReceiverUUID() {
        return receiverUUID;
    }

    public UUID getRequesterUUID() {
        return requesterUUID;
    }
}
