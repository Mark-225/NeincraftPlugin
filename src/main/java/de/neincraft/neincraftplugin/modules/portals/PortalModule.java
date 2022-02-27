package de.neincraft.neincraftplugin.modules.portals;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.commands.InjectCommand;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.LocationData;
import de.neincraft.neincraftplugin.modules.portals.dto.Portal;
import de.neincraft.neincraftplugin.modules.portals.dto.embeddable.PortalCoordinates;
import de.neincraft.neincraftplugin.modules.portals.repository.PortalRepository;
import de.neincraft.neincraftplugin.util.Lang;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

@NeincraftModule(id = "PortalModule", requiredModules = {"database"})
public class PortalModule extends AbstractModule implements Listener {

    private final ConcurrentHashMap<String, Portal> loadedPortals = new ConcurrentHashMap<>();

    @InjectCommand("portal")
    private PortalCommand portalCommand;

    @Override
    protected boolean initModule() {
        try(PortalRepository repository = PortalRepository.getRepository()){
            if(repository != null)
                loadedPortals.putAll(repository.findAll().stream().collect(Collectors.toMap(Portal::getName, portal -> portal)));
        }catch(Exception e){
            getLogger().log(Level.WARNING, e, () -> "Failed to load Portals");
        }
        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
        Bukkit.getScheduler().runTaskTimerAsynchronously(NeincraftPlugin.getInstance(), () ->{
            List<Runnable> particleSpawners = new ArrayList<>();
            for(Portal portal : loadedPortals.values()){
                if(!portal.isAreaDefined()) continue;
                Vector3d location = portal.getCenterPoint();
                double offsetX = portal.getCenterPoint().getX() - portal.getMinCorner().getX();
                double offsetY = portal.getCenterPoint().getY() - portal.getMinCorner().getY();
                double offsetZ = portal.getCenterPoint().getZ() - portal.getMinCorner().getZ();
                long volume = portal.getVolume();
                if(volume < 2) volume = 2;
                if(volume > 50) volume = 50;
                int particleCount = (int) volume * 5;
                particleSpawners.add(() ->{
                   World world = Bukkit.getWorld(portal.getWorld());
                   if(world == null) return;
                   world.spawnParticle(Particle.PORTAL, location.getX(), location.getY(), location.getZ(), particleCount, offsetX, offsetY, offsetZ);
                });
            }
            Bukkit.getScheduler().runTask(NeincraftPlugin.getInstance(), () ->{
                particleSpawners.forEach(Runnable::run);
            });
        }, 20, 20);
        return true;
    }

    public List<String> getLoadedPortalNames(){
        return List.copyOf(loadedPortals.keySet());
    }

    public Portal getPortal(String name){
        return loadedPortals.get(name.toLowerCase());
    }

    public Portal createPortal(String name){
        Portal portal = new Portal(name.toLowerCase());
        loadedPortals.put(portal.getName(), portal);
        persist(portal);
        return portal;
    }

    public void persist(Portal portal){
        try(PortalRepository repository = PortalRepository.getRepository()){
            if(repository != null) {
                repository.save(portal);
                repository.commit();
            }
        }catch(Exception e){
            getLogger().log(Level.WARNING, e, () -> "Portal could not be saved");
        }
    }

    public void remove(String name){
        Portal portal = getPortal(name);
        if(portal != null){
            try(PortalRepository repository = PortalRepository.getRepository()){
                if(repository != null) {
                    repository.delete(portal);
                    repository.commit();
                    loadedPortals.remove(name.toLowerCase());
                }
            }catch(Exception e){
                getLogger().log(Level.WARNING, e, () -> "Portal could not be deleted");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event){
        if(event.isCancelled() || !event.hasChangedBlock()) return;
        Location loc = event.getTo();
        for(Portal portal : loadedPortals.values()){
            if(portal.getTarget() == null) continue;
            if(!event.getPlayer().getWorld().getName().equals(portal.getWorld())) continue;
            if(!portal.isInside(Vector3i.from(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))) continue;
            if(!event.getPlayer().hasPermission("neincraft.portal.use." + portal.getName())) return;
            World w = Bukkit.getWorld(portal.getTargetWorld());
            if(w == null) return;
            LocationData target = portal.getTarget();
            NeincraftUtils.teleportToLocation(event.getPlayer(), new Location(w, target.getX(), target.getY(), target.getZ(), target.getYaw(), target.getPitch()));
        }
    }

    private String getToolPortal(ItemStack tool){
        return tool.getItemMeta().getPersistentDataContainer().get(portalCommand.portalEditName, PersistentDataType.STRING);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        if(event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(event.getItem() == null || event.getItem().getType() != Material.STICK) return;
        String portalId = getToolPortal(event.getItem());
        if(portalId == null) return;
        Portal portal = getPortal(portalId);
        if(portal == null) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        if(!player.hasPermission("neincraft.commands.admin.portal")) return;
        switch(event.getAction()){
            case LEFT_CLICK_BLOCK -> {
                Block block = event.getClickedBlock();
                if(block == null) return;
                if(portal.getMinCorner() == null || portal.isAreaDefined() || !block.getWorld().getName().equals(portal.getWorld())){
                    portal.setMinCorner(new PortalCoordinates(block.getX(), block.getY(), block.getZ()));
                    portal.setWorld(block.getWorld().getName());
                    portal.setMaxCorner(null);
                    player.sendMessage(Lang.PORTAL_FIRST_POS_SET.getComponent(player));
                }else {
                    PortalCoordinates firstPos = portal.getMinCorner();
                    PortalCoordinates minPos = new PortalCoordinates(Math.min(firstPos.getX(), block.getX()), Math.min(firstPos.getY(), block.getY()), Math.min(firstPos.getZ(), block.getZ()));
                    PortalCoordinates maxPos = new PortalCoordinates(Math.max(firstPos.getX(), block.getX()), Math.max(firstPos.getY(), block.getY()), Math.max(firstPos.getZ(), block.getZ()));
                    portal.setMinCorner(minPos);
                    portal.setMaxCorner(maxPos);
                    persist(portal);
                    player.sendMessage(Lang.PORTAL_AREA_DEFINED.getComponent(player));
                }
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                Location loc = player.getLocation();
                portal.setTarget(new LocationData(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
                portal.setTargetWorld(loc.getWorld().getName());
                persist(portal);
                player.sendMessage(Lang.PORTAL_DEST_SET.getComponent(player));
            }
        }
    }

    @Override
    public void unload() {

    }
}
