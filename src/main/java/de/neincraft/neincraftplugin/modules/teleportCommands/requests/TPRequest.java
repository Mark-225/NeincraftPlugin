package de.neincraft.neincraftplugin.modules.teleportCommands.requests;

import org.bukkit.entity.Player;

public abstract class TPRequest {

    private final Player sender;
    private final Player receiver;

    public TPRequest(Player sender, Player receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public Player getSender() {
        return sender;
    }

    public Player getReceiver() {
        return receiver;
    }

    public abstract void execute();

}
