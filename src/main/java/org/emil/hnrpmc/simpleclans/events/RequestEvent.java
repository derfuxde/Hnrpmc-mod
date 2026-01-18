package org.emil.hnrpmc.simpleclans.events;

import org.emil.hnrpmc.simpleclans.Request;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author NeT32
 */
public class RequestEvent extends Event {
    private final Request RequestProcess;

    public RequestEvent(Request RequestProcess) {
        this.RequestProcess = RequestProcess;
    }

    public Request getRequest() {
        return this.RequestProcess;
    }

}
