package de.neincraft.neincraftplugin.modules.timber;

import com.destroystokyo.paper.MaterialTags;
import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.commands.InjectCommand;
import de.neincraft.neincraftplugin.util.BlockBatchChangedEvent;
import de.neincraft.neincraftplugin.util.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

@NeincraftModule(id = "Timber")
public class TimberModule extends AbstractModule implements Listener {

    @InjectCommand("timber")
    private TimberCommand timberCommand;

    @Override
    protected boolean initModule() {
        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
        return true;
    }

    private boolean isTimberEnabled(Player p){
        return Byte.valueOf((byte) 1).equals(p.getPersistentDataContainer().get(timberCommand.timberStateKey, PersistentDataType.BYTE));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event){
        if(event.isCancelled()) return;
        if(!event.getPlayer().isSneaking()) return;
        if(!MaterialTags.AXES.isTagged(event.getPlayer().getEquipment().getItemInMainHand().getType())) return;
        if(!isTimberEnabled(event.getPlayer())) return;
        int searchLimit = 0;
        int xzOffset = 0;
        Material treeType = event.getBlock().getType();
        List<Material> toBreak = null;
        switch(treeType) {
            case OAK_LOG -> {
                searchLimit = 40;
                xzOffset = 3;
                toBreak = List.of(Material.OAK_LOG);
                }
            case BIRCH_LOG -> {
                searchLimit = 20;
                xzOffset = 0;
                toBreak = List.of(Material.BIRCH_LOG);
                }
            case DARK_OAK_LOG -> {
                searchLimit = 80;
                xzOffset = 3;
                toBreak = List.of(Material.DARK_OAK_LOG);
                }
            case ACACIA_LOG -> {
                searchLimit = 20;
                xzOffset = 3;
                toBreak = List.of(Material.ACACIA_LOG);
                }
            case JUNGLE_LOG -> {
                searchLimit = 150;
                xzOffset = 7;
                toBreak = List.of(Material.JUNGLE_LOG);
                }
            case SPRUCE_LOG -> {
                searchLimit = 130;
                xzOffset = 1;
                toBreak = List.of(Material.SPRUCE_LOG);
                }
            case MUSHROOM_STEM, RED_MUSHROOM_BLOCK, BROWN_MUSHROOM_BLOCK -> {
                searchLimit = 60;
                xzOffset = 3;
                toBreak = List.of(Material.MUSHROOM_STEM, Material.RED_MUSHROOM_BLOCK, Material.BROWN_MUSHROOM_BLOCK);
                }
            case CRIMSON_STEM -> {
                searchLimit = 100;
                xzOffset = 4;
                toBreak = List.of(Material.CRIMSON_STEM, Material.NETHER_WART_BLOCK);
                }
            case WARPED_STEM -> {
                searchLimit = 100;
                xzOffset = 4;
                toBreak = List.of(Material.WARPED_STEM, Material.WARPED_WART_BLOCK);
                }
            case CHERRY_LOG -> {
                searchLimit = 40;
                xzOffset = 6;
                toBreak = List.of(Material.CHERRY_LOG);
            }
            case MANGROVE_LOG, MANGROVE_ROOTS, MUDDY_MANGROVE_ROOTS -> {
                searchLimit = 70;
                xzOffset = 6;
                toBreak = List.of(Material.MANGROVE_LOG, Material.MANGROVE_ROOTS, Material.MUDDY_MANGROVE_ROOTS);
            }
            default -> {
                return;
            }
        }

        List<Block> toRemove = new ArrayList<>();
        fillRec(event.getBlock(), event.getBlock(), toRemove, xzOffset, searchLimit, toBreak);
        BlockBatchChangedEvent bcbEvent = new BlockBatchChangedEvent(event.getBlock(), toRemove);
        Bukkit.getPluginManager().callEvent(bcbEvent);
        if(bcbEvent.isCancelled()) return;
        final List<Block> finalBlocks = bcbEvent.getChangingBlocks();
        finalBlocks.sort((b1, b2) ->{
            int heightDif = b1.getY() - b2.getY();
            if(heightDif == 0) {
                int xDif = b1.getX() - b2.getX();
                if(xDif == 0) {
                    return b1.getZ() - b2.getZ();
                }else {
                    return xDif;
                }
            }else {
                return heightDif;
            }
        });
        for(int i = 0; i < finalBlocks.size(); i++){
            final int index = i;
            Bukkit.getScheduler().runTaskLater(NeincraftPlugin.getInstance(), () -> finalBlocks.get(index).breakNaturally(), i+1);
        }
    }

    @EventHandler
    public void onItemChange(PlayerItemHeldEvent event){
        ItemStack is;
        if((is = event.getPlayer().getInventory().getItem(event.getNewSlot())) == null || !MaterialTags.AXES.isTagged(is.getType())) return;
        if(!isTimberEnabled(event.getPlayer())) return;
        event.getPlayer().sendActionBar(Lang.TIMBER_WARNING.getComponent(event.getPlayer()));
    }
    
    
    private void fillRec(Block b, Block origin, List<Block> marked, int xzOffset, int searchLimit, List<Material> toBreak) {
        if(marked.contains(b) || marked.size() >= searchLimit || Math.abs(b.getX() - origin.getX()) > xzOffset || Math.abs(b.getZ() - origin.getZ()) > xzOffset) {
            return;
        }
        marked.add(b);
        for(int x = -1; x <= 1; x ++) {
            for(int z = -1; z <= 1; z++) {
                for(int y = -1; y <= 1; y++) {
                    if(!(x == 0 && y == 0 && z == 0)) {
                        Block next = origin.getWorld().getBlockAt(b.getX() + x, b.getY() + y, b.getZ() + z);
                        if(toBreak.contains(next.getType())) {
                            fillRec(next, origin, marked, xzOffset, searchLimit, toBreak);
                        }
                    }
                }
            }
        }
    }
    

    @Override
    public void unload() {

    }
}
