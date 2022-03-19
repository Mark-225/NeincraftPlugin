package de.neincraft.neincraftplugin.modules.teleportCommands;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.commands.InjectCommand;
import de.neincraft.neincraftplugin.modules.teleportCommands.requests.TPRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NeincraftModule(id = "TeleportCommands")
public class TeleportCommands extends AbstractModule {

    @InjectCommand("tpa")
    private TPACommand tpaCommand;

    private final Map<Player, TPRequest> sentRequests = new HashMap<>();

    public boolean createRequest(TPRequest request){
        if(sentRequests.containsKey(request.getSender())) return false;
        sentRequests.put(request.getSender(), request);
        Bukkit.getScheduler().runTaskLater(NeincraftPlugin.getInstance(), () -> {
            sentRequests.remove(request.getSender(), request);
        }, 600);
        return true;
    }

    public boolean acceptRequest(Player sender, Player receiver){
        TPRequest request = sentRequests.get(sender);
        if(request == null) return false;
        if(request.getReceiver() != receiver) return false;
        request.execute();
        sentRequests.remove(sender);
        return true;
    }

    public List<TPRequest> getOpenRequests(Player receiver){
        return sentRequests.values().stream().filter(r -> r.getReceiver() == receiver).toList();
    }

    @Override
    protected boolean initModule() {
        return true;
    }

    @Override
    public void unload() {

    }
}
