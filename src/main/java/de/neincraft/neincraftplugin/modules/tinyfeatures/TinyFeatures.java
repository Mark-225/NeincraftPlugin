package de.neincraft.neincraftplugin.modules.tinyfeatures;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.blockentityproperties.BEProperty;
import de.neincraft.neincraftplugin.modules.blockentityproperties.BlockEntityProperties;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@NeincraftModule(id = "TinyFeatures")
public class TinyFeatures extends AbstractModule implements Listener {

    @Override
    protected boolean initModule() {
        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
        return true;
    }

    //Anvil Dispensers

    @EventHandler(priority = EventPriority.HIGH)
    public void onDispenserDispenseAnvil(BlockPreDispenseEvent event){
        if(event.isCancelled()) return;
        if(event.getBlock().getType() != Material.DISPENSER) return;
        if(event.getBlock().getBlockData() instanceof Directional directional && event.getBlock().getState(false) instanceof Dispenser dispenser) {
            ItemStack item = dispenser.getInventory().getItem(event.getSlot());
            if(item == null || item.getType() != Material.ANVIL) return;
            event.setCancelled(true);
            Block target = event.getBlock().getRelative(directional.getFacing());
            if(target.getType() != Material.AIR && target.getType() != Material.CAVE_AIR) return;
            target.setType(Material.ANVIL);
            event.getBlock().getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
            Optional<BlockEntityProperties> beProperties = AbstractModule.getInstance(BlockEntityProperties.class);
            if(beProperties.isEmpty() || !beProperties.get().getProperty(dispenser, BEProperty.INFDISP)) item.setAmount(item.getAmount()-1);
        }
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
        if(event.getHand() != EquipmentSlot.HAND || !event.getPlayer().isSneaking()) return;
        if(!Tag.SIGNS.isTagged(event.getClickedBlock().getType())) return;
        if(!event.getPlayer().hasPermission("neincraft.editsigns")) return;
        if(event.getClickedBlock().getState(false) instanceof Sign sign){
            event.getPlayer().openSign(sign);
            event.setCancelled(true);
        }
    }

    @Override
    public void unload() {

    }
}
