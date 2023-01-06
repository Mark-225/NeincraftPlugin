package de.neincraft.neincraftplugin.modules.blockentityproperties;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.plots.Plot;
import de.neincraft.neincraftplugin.modules.plots.PlotModule;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.ChunkKey;
import de.neincraft.neincraftplugin.util.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class BlockEditMenu implements Listener {

    private HashMap<Integer, BEProperty> settingIndices = new HashMap<>();
    private final Player player;
    private final Inventory inventory;
    private final TileState editedBlock;
    private final BlockEntityProperties module;


    public BlockEditMenu(Player player, TileState block, BlockEntityProperties module){
        this.player = player;
        this.editedBlock = block;
        this.module = module;
        inventory = Bukkit.createInventory(null, 27, Lang.BE_HEADER.getComponent(player));
    }

    public void startListening(){
        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event){
        if(event.getInventory() != inventory) return;
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(event.getView().getTopInventory() != inventory) return;
        event.setCancelled(true);
        if(event.getClickedInventory() != inventory) return;
        if(event.getClick() != ClickType.LEFT) return;
        if(event.getSlot() < 9) return;
        int rootSlot = event.getSlot() % 9;
        boolean clickedValue = true;
        if(event.getSlot() >= 18) clickedValue = false;
        if(!settingIndices.containsKey(rootSlot)) return;
        setSetting(settingIndices.get(rootSlot), clickedValue);
        refreshInventory();
    }

    public void setSetting(BEProperty property, boolean value){
        if(editedBlock instanceof Container container && container.getInventory().getHolder() instanceof DoubleChest doubleChest){
            if(doubleChest.getLeftSide() != null && doubleChest.getRightSide() != null) {
                setSetting((TileState) doubleChest.getLeftSide(false), property, value);
                setSetting((TileState) doubleChest.getRightSide(false), property, value);
            }
            return;
        }
        setSetting(editedBlock, property, value);
    }

    public void setSetting(TileState block, BEProperty property, boolean value){
        if(!block.getChunk().isLoaded()) return;
        if(!player.hasPermission("neincraft.blockproperties." + property.getRequiredPermission())) return;
        Optional<PlotModule> oPm = AbstractModule.getInstance(PlotModule.class);
        if(oPm.isEmpty()) return;
        Optional<Plot> oPlot = oPm.get().getPlotAtChunk(ChunkKey.fromChunk(block.getChunk()));
        if(oPlot.isEmpty()) return;
        if(!player.getUniqueId().equals(oPlot.get().getPlotData().getOwner()) && !player.hasPermission("neincraft.commands.admin.plotadmin")) return;
        NamespacedKey key = new NamespacedKey(NeincraftPlugin.getInstance(), property.getKeyIdentifier());
        if(value){
            block.getPersistentDataContainer().set(key, PersistentDataType.LONG, oPlot.get().getPlotData().getId());
        }else{
            block.getPersistentDataContainer().remove(key);
        }
    }

    public void refreshInventory(){
        inventory.clear();
        settingIndices.clear();
        int rootIndex = 0;
        List<BEProperty> properties = Arrays.stream(BEProperty.values()).filter(beProperty -> beProperty.getTargetPredicate().test(editedBlock) && player.hasPermission("neincraft.blockproperties." + beProperty.getRequiredPermission())).toList();
        for(int i = 0; i < 9 && i < properties.size(); i++){
            BEProperty property = properties.get(i);
            insertProperty(i, property);
        }
    }

    private void insertProperty(int rootIndex, BEProperty property){
        inventory.setItem(rootIndex, property.getDisplayItem().apply(player));
        boolean state = module.getProperty(editedBlock, property);
        inventory.setItem(rootIndex + 9, makeToggle(true, state, player));
        inventory.setItem(rootIndex + 18, makeToggle(false, !state, player));
        settingIndices.put(rootIndex, property);
    }

    private static ItemStack makeToggle(boolean value, boolean active, Player player){
        ItemStack is = new ItemStack(value ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta im = is.getItemMeta();
        im.displayName(value ? Lang.BE_ACTIVATE.getComponent(player) : Lang.BE_DEACTIVATE.getComponent(player));
        if(active) im.addEnchant(Enchantment.DURABILITY, 1, true);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        is.setItemMeta(im);
        return is;
    }

    public void openInventory(){
        if(!player.isOnline()) return;
        startListening();
        refreshInventory();
        player.openInventory(inventory);
    }



}
