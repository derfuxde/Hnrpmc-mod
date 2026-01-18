package org.emil.hnrpmc.simpleclans.events;

import org.emil.hnrpmc.simpleclans.Request;
import net.neoforged.bus.api.Event;

/**
 *
 * @author NeT32
 */
public class RequestFinishedEvent extends Event {

    private final Request RequestProcess;

    public RequestFinishedEvent(Request RequestProcess) {
        this.RequestProcess = RequestProcess;
    }

    public Request getRequest() {
        return this.RequestProcess;
    }

}
