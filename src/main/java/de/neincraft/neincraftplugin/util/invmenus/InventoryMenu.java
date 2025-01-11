package de.neincraft.neincraftplugin.util.invmenus;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public abstract class InventoryMenu implements Listener {

    protected Inventory inventory;

    public InventoryMenu(int rows, Component title) {
        if(rows < 1) rows = 1;
        if(rows > 6) rows = 6;
        inventory = Bukkit.createInventory(null, rows * 9, title);
        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getInventory() != inventory) return;
        event.setCancelled(true);
        if(inventory == event.getClickedInventory())
            handleInventoryClick(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        closeSafely();
    }

    private void closeSafely(){
        inventory.clear();
        HandlerList.unregisterAll(this);
        inventory.close();
    }

    protected abstract void handleInventoryClick(InventoryClickEvent event);


}
