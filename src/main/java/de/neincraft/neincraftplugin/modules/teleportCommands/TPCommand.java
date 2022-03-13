package de.neincraft.neincraftplugin.modules.teleportCommands;

import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.commands.SimpleTabCompleter;
import org.bukkit.command.CommandExecutor;

public abstract class TPCommand implements CommandExecutor, SimpleTabCompleter {

    protected TeleportCommands teleportModule;

    public TPCommand(){
        teleportModule = AbstractModule.getInstance(TeleportCommands.class).orElse(null);
    }

}
