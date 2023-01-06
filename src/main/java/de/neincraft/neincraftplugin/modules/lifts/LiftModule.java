package de.neincraft.neincraftplugin.modules.lifts;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.util.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

@NeincraftModule(id = "LiftModule")
public class LiftModule extends AbstractModule implements Listener {

    @Override
    protected boolean initModule() {
        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
        return true;
    }

    private boolean isLift(Block base){
        Block head;
        return base.getType() == Material.IRON_BLOCK && base.getRelative(BlockFace.UP).getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE && !(head = base.getRelative(BlockFace.UP, 2)).getType().isCollidable() && !head.isLiquid();
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event){
        if(!isLift(event.getFrom().getBlock().getRelative(BlockFace.DOWN))) return;
        event.setCancelled(true);
        for (Block block = event.getFrom().getBlock().getRelative(BlockFace.UP, 3); block.getY() < block.getWorld().getMaxHeight() - 2; block = block.getRelative(BlockFace.UP)){
            if(isLift(block)) {
                Location target = new Location(block.getWorld(), block.getX() +0.5d, block.getY()+1d, block.getZ()+0.5d, event.getFrom().getYaw(), event.getFrom().getPitch());
                Bukkit.getScheduler().runTaskLater(NeincraftPlugin.getInstance(), () -> event.getPlayer().teleport(target), 0);
                event.getPlayer().setVelocity(new Vector(0, 0, 0));
                event.getPlayer().playSound(event.getPlayer(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
                return;
            }
        }
        event.getPlayer().playSound(event.getPlayer(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1, 1);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event){
        if(!event.isSneaking()) return;
        if(!isLift(event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN))) return;
        for (Block block = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN, 4); block.getY() >= block.getWorld().getMinHeight(); block = block.getRelative(BlockFace.DOWN)){
            if(isLift(block)) {
                Location target = new Location(block.getWorld(), block.getX() +0.5d, block.getY()+1d, block.getZ()+0.5d, event.getPlayer().getLocation().getYaw(), event.getPlayer().getLocation().getPitch());
                Bukkit.getScheduler().runTaskLater(NeincraftPlugin.getInstance(), () -> event.getPlayer().teleport(target), 0);
                event.getPlayer().playSound(event.getPlayer(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
                return;
            }
        }
        event.getPlayer().playSound(event.getPlayer(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1, 1);
    }

    @EventHandler
    public void onPlayerEnterLift(PlayerInteractEvent event){
        if(event.getAction() != Action.PHYSICAL) return;
        if(!isLift(event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN))) return;
        event.getPlayer().sendActionBar(Lang.LIFT_ENTER.getComponent(event.getPlayer()));
    }

    @Override
    public void unload() {

    }
}
