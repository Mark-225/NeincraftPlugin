package de.neincraft.neincraftplugin.modules.commands;

import de.neincraft.neincraftplugin.NeincraftPlugin;

import java.util.logging.Logger;

public abstract class NeincraftCommand {

    private final Logger logger = NeincraftPlugin.getInstance().getLogger();

    public NeincraftCommand(){}

    protected Logger getLogger(){
        return logger;
    }
}
