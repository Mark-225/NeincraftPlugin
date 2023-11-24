package de.neincraft.neincraftplugin.modules.blockentityproperties;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.plots.Plot;
import de.neincraft.neincraftplugin.modules.plots.PlotModule;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.ChunkKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Optional;

@NeincraftModule(id = "BlockEntityProperties")
public class BlockEntityProperties extends AbstractModule implements Listener {

    @Override
    protected boolean initModule() {
        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
        return true;
    }

    public boolean getProperty(TileState block, BEProperty property){
        Optional<Plot> oPlot = AbstractModule.getInstance(PlotModule.class).flatMap(pm -> pm.getPlotAtChunk(ChunkKey.fromChunk(block.getChunk())));
        if(oPlot.isEmpty()) return false;
        NamespacedKey key = new NamespacedKey(NeincraftPlugin.getInstance(), property.getKeyIdentifier());
        if(!block.getPersistentDataContainer().has(key)) return false;
        Long id = block.getPersistentDataContainer().get(key, PersistentDataType.LONG);
        return id != null && id == oPlot.get().getPlotData().getId();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(!player.isSneaking() || event.getHand() != EquipmentSlot.HAND) return;
        if(event.getItem() == null || event.getItem().getType() != Material.STICK) return;
        Block b = event.getClickedBlock();
        BlockState bs = b.getState(false);
        if(!(bs instanceof TileState)) return;
        TileState ts = (TileState) bs;
        if(Arrays.stream(BEProperty.values()).noneMatch(beProperty -> beProperty.getTargetPredicate().test(ts))) return;
        event.setCancelled(true);
        Optional<Plot> oPlot = AbstractModule.getInstance(PlotModule.class).flatMap(pm -> pm.getPlotAtChunk(ChunkKey.fromChunk(ts.getChunk())));
        if(oPlot.isEmpty()) return;
        if(!player.getUniqueId().equals(oPlot.get().getPlotData().getOwner()) && !player.hasPermission("neincraft.commands.admin.plotadmin")) return;
        BlockEditMenu bem = new BlockEditMenu(player, ts, this);
        bem.openInventory();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDispense(BlockDispenseEvent event){
        if(event.isCancelled()) return;
        if(event.getBlock().getState(false) instanceof InventoryHolder holder && ((holder instanceof Dispenser dispenser && getProperty(dispenser, BEProperty.INFDISP)) || (holder instanceof Dropper dropper && getProperty(dropper, BEProperty.INFDISP)))) {
            final ItemStack dispensedItem = event.getItem();
            final Inventory inv = holder.getInventory();
            Bukkit.getScheduler().runTask(NeincraftPlugin.getInstance(), () -> inv.addItem(dispensedItem));
        }
    }

    @EventHandler
    public void onDropperMoveItem(InventoryMoveItemEvent event){
        if(event.getSource().getHolder(false) instanceof Dropper dropper){
            if(!getProperty(dropper, BEProperty.INFDISP)) return;
            ItemStack is = event.getItem();
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(NeincraftPlugin.getInstance(), () -> event.getDestination().addItem(event.getItem()));
        }
    }

    @Override
    public void unload() {

    }
}
