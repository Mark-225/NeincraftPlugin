package de.neincraft.neincraftplugin.modules.playerstats;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.Module;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.playerstats.chat.NeincraftChatRenderer;
import de.neincraft.neincraftplugin.modules.playerstats.dto.PlayerData;
import de.neincraft.neincraftplugin.util.Lang;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

@NeincraftModule(id = "PlayerStats")
public class PlayerStats extends Module implements Listener {

    private ConcurrentHashMap<UUID, PlayerData> playerStats = new ConcurrentHashMap<>();
    private HashMap<UUID, Long> lastSeen = new HashMap<>();
    private NeincraftChatRenderer chatRenderer;

    @Override
    protected boolean initModule() {
        chatRenderer = new NeincraftChatRenderer(this);
        Bukkit.getScheduler().runTaskTimer(NeincraftPlugin.getInstance(), () ->{
            long time = System.currentTimeMillis();
            List<UUID> expired = playerStats.entrySet().stream().filter(entry -> entry.getValue().getExpiryTimestamp() <= time).map(Map.Entry::getKey).toList();
            expired.forEach(uuid -> playerStats.remove(uuid));
        }, 300000, 300000);

        Bukkit.getScheduler().runTaskTimer(NeincraftPlugin.getInstance(), () ->{

        }, 400, 400);

        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());

        return true;
    }

    public synchronized PlayerData getOrCreate(UUID uuid){
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

    public synchronized void savePlayerData(PlayerData pd){
        PlayerDataRepo repo = PlayerDataRepo.getRepository();
        if(repo == null){
            getLogger().log(Level.WARNING, "Failed to persist PlayerData. Repository could not be created.");
            return;
        }
        repo.save(pd);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if(!event.getPlayer().hasPlayedBefore()) {
            NeincraftUtils.formattedBroadcast(Lang.FIRST_JOIN, MessageType.SYSTEM, "player", event.getPlayer().getName());
            event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());
        }
        lastSeen.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event){
        event.renderer(chatRenderer);
    }

    @Override
    public void unload() {

    }
}
