package de.neincraft.neincraftplugin.modules.playerstats;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.Module;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.playerstats.dto.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

@NeincraftModule(id = "PlayerStats")
public class PlayerStats extends Module {

    BukkitTask cleanupTask;
    private HashMap<UUID, PlayerData> playerStats = new HashMap<>();

    @Override
    protected boolean initModule() {
        cleanupTask = Bukkit.getScheduler().runTaskTimer(NeincraftPlugin.getInstance(), () ->{
            long time = System.currentTimeMillis();
            List<UUID> expired = playerStats.entrySet().stream().filter(entry -> entry.getValue().getExpiryTimestamp() <= time).map(Map.Entry::getKey).toList();
            expired.forEach(uuid -> playerStats.remove(uuid));
        }, 300000, 300000);
        return true;
    }

    public PlayerData getOrCreate(UUID uuid){
        PlayerData pd = playerStats.get(uuid);
        PlayerDataRepo repo;
        if(pd == null && (repo = PlayerDataRepo.getRepository()) != null) {
            pd = repo.getOrCreate(uuid);
            playerStats.put(uuid, pd);
            repo.close();
        }
        if(pd != null)
            pd.setExpiryTimestamp(System.currentTimeMillis() + 300000);
        return pd;
    }

    public void savePlayerData(PlayerData pd){
        PlayerDataRepo repo = PlayerDataRepo.getRepository();
        if(repo == null){
            getLogger().log(Level.WARNING, "Failed to persist PlayerData. Repository could not be created.");
            return;
        }
        repo.save(pd);
    }

    @Override
    public void unload() {
        if(!cleanupTask.isCancelled())
            cleanupTask.cancel();
    }
}
