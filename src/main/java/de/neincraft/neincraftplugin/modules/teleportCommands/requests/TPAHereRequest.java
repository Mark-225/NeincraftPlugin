package de.neincraft.neincraftplugin.modules.teleportCommands.requests;

import de.neincraft.neincraftplugin.util.NeincraftUtils;
import org.bukkit.entity.Player;

public class TPAHereRequest extends TPRequest{

    public TPAHereRequest(Player sender, Player receiver) {
        super(sender, receiver);
    }

    @Override
    public void execute() {
        NeincraftUtils.teleportToLocation(getReceiver(), getSender().getLocation());
    }
}
