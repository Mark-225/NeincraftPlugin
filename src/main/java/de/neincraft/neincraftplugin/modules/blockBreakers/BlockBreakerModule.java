package de.neincraft.neincraftplugin.modules.blockBreakers;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.plots.PlotModule;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.ChunkKey;
import de.neincraft.neincraftplugin.modules.plots.util.PlotUtils;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Random;

@NeincraftModule(
        id="blockBreaker",
        isVital=false,
        requiredModules = {"plots"}
)
public class BlockBreakerModule extends AbstractModule implements Listener {

    PlotModule plotModule = null;
    Random random;
    ItemStack stick;
    @Override
    protected boolean initModule() {
        AbstractModule.getInstance(PlotModule.class).ifPresent(pm -> {
            plotModule = pm;
            random = new Random();
            stick = new ItemStack(Material.STICK);
            Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
        });
        return true;
    }

    @EventHandler
    public void onDispenseItem(BlockPreDispenseEvent event){
        Block dispenserBlock = event.getBlock();
        if(Material.DISPENSER != dispenserBlock.getType()) return;
        ItemStack dispensedItem = event.getItemStack();
        if(!Tag.ITEMS_TOOLS.isTagged(dispensedItem.getType())) return;
        event.setCancelled(true);
        Dispenser dispenserData = (Dispenser) dispenserBlock.getBlockData();
        BlockFace face = dispenserData.getFacing();
        Block targetBlock = dispenserBlock.getRelative(face);
        if(targetBlock.getType().isAir() || targetBlock.getType().getHardness() < 0 || targetBlock.getBlockData().getDestroySpeed(stick) >= targetBlock.getBlockData().getDestroySpeed(dispensedItem, false)){
            effectFail(dispenserBlock);
            return;
        }

        ChunkKey from = ChunkKey.fromChunk(dispenserBlock.getChunk());
        ChunkKey to = ChunkKey.fromChunk(targetBlock.getChunk());
        if(!PlotUtils.canPropagate(plotModule, from, to)){
            effectFail(dispenserBlock);
            return;
        }
        targetBlock.breakNaturally(dispensedItem, true);
        int unbreakingLevels = dispensedItem.getEnchantmentLevel(Enchantment.DURABILITY);
        boolean decreaseDurability = unbreakingLevels <= 0 || random.nextDouble() < 1d / (double) (unbreakingLevels + 1);
        if(decreaseDurability && dispensedItem.getItemMeta() instanceof Damageable damageable){
            int newDamage = damageable.getDamage() + 1;
            if(newDamage >= dispensedItem.getType().getMaxDurability()){
                dispensedItem.setAmount(dispensedItem.getAmount() - 1);
                dispenserBlock.getWorld().playSound(dispenserBlock.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            }else{
                damageable.setDamage(newDamage);
                dispensedItem.setItemMeta(damageable);
            }
        }
    }

    private void effectFail(Block dispenser){
        dispenser.getWorld().playSound(dispenser.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1, 1);
    }

    @Override
    public void unload() {

    }
}
