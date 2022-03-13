package de.neincraft.neincraftplugin.modules.dragonfights;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import io.papermc.paper.event.block.DragonEggFormEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

@NeincraftModule(id = "DragonFights")
public class DragonFights extends AbstractModule implements Listener {
    @Override
    protected boolean initModule() {
        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDragonDefeat(DragonEggFormEvent event){
        event.setCancelled(false);
    }

    @Override
    public void unload() {

    }
}
