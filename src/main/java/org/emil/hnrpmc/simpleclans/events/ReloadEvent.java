package org.emil.hnrpmc.simpleclans.events;

import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

public class ReloadEvent extends Event {
    
    private final CommandSourceStack sender;
    
    public ReloadEvent(CommandSourceStack sender) {
        this.sender = sender;
    }
    
    public CommandSourceStack getSender() {
        return sender;
    }
}