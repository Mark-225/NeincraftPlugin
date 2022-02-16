package de.neincraft.neincraftplugin;

import de.neincraft.neincraftplugin.modules.ModuleLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class NeincraftPlugin extends JavaPlugin {

    private static NeincraftPlugin instance;

    public static NeincraftPlugin getInstance(){
        return instance;
    }

    private ModuleLoader moduleLoader;

    @Override
    public void onEnable() {
        instance = this;
        moduleLoader = new ModuleLoader();
        
        getLogger().log(Level.INFO, "Loading modules...");
        boolean success = moduleLoader.loadModules();
        if(!success){
            getLogger().log(Level.SEVERE, "Important modules failed to load! Shutting down.");
            Bukkit.shutdown();
            return;
        }
        getLogger().log(Level.INFO, "Modules loaded!");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Unloading modules...");
        moduleLoader.unloadModules();
    }
}
