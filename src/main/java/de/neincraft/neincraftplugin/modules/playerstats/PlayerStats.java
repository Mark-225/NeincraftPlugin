package de.neincraft.neincraftplugin.modules.playerstats;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.commands.InjectCommand;
import de.neincraft.neincraftplugin.modules.playerstats.chat.NeincraftChatRenderer;
import de.neincraft.neincraftplugin.modules.playerstats.dto.PlayerData;
import de.neincraft.neincraftplugin.util.Lang;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

@NeincraftModule(id = "PlayerStats")
public class PlayerStats extends AbstractModule implements Listener {

    private final ConcurrentHashMap<UUID, PlayerData> playerStats = new ConcurrentHashMap<>();
    private final HashMap<UUID, Long> lastSeen = new HashMap<>();
    private final List<UUID> afkPlayers = new ArrayList<>();
    private final HashMap<UUID, Integer> points = new HashMap<>();
    private NeincraftChatRenderer chatRenderer;
    private ScheduledFuture<?> statTask;

    @InjectCommand("stats")
    private StatsCommand statsCommand;

    @InjectCommand("label")
    private LabelCommand labelCommand;

    @Override
    protected boolean initModule() {
        chatRenderer = new NeincraftChatRenderer(this);
        Bukkit.getScheduler().runTaskTimer(NeincraftPlugin.getInstance(), () ->{
            long time = System.currentTimeMillis();
            List<UUID> expired = playerStats.entrySet().stream().filter(entry -> entry.getValue().getExpiryTimestamp() <= time).map(Map.Entry::getKey).toList();
            expired.forEach(playerStats::remove);
        }, 300000, 300000);

        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());

        ScheduledExecutorService statExecutor = Executors.newScheduledThreadPool(1);
        statTask = statExecutor.scheduleAtFixedRate(this::collectStats, 20, 20, TimeUnit.SECONDS);

        return true;
    }

    private void collectStats(){
        Bukkit.getScheduler().runTask(NeincraftPlugin.getInstance(), this::collectStatsSync);
    }

    private void collectStatsSync(){
        long time = System.currentTimeMillis();
        List<PlayerData> toSave = new ArrayList<>();
        for(Player p : Bukkit.getOnlinePlayers()){
            if(!lastSeen.containsKey(p.getUniqueId()) || afkPlayers.contains(p.getUniqueId())) continue;
            if(time - lastSeen.get(p.getUniqueId()) >= 300_000){
                Lang afkMessage = Lang.PLAYER_AFK;
                Random rand = new Random();
                if(rand.nextDouble() < 0.1d) {
                    List<Lang> messages = List.of(Lang.PLAYER_AFK_RND_1, Lang.PLAYER_AFK_RND_2, Lang.PLAYER_AFK_RND_3, Lang.PLAYER_AFK_RND_4, Lang.PLAYER_AFK_RND_5);
                    afkMessage = messages.get(rand.nextInt(0, messages.size()));
                }
                NeincraftUtils.formattedBroadcast(afkMessage, MessageType.SYSTEM, "player", p.getName());
                afkPlayers.add(p.getUniqueId());
            }
            PlayerData pd = getOrCreate(p.getUniqueId());
            pd.setSecondsPlayed(pd.getSecondsPlayed() + 20);
            pd.setPoints(pd.getPoints() + 5 + points.getOrDefault(p.getUniqueId(), 0));
            toSave.add(pd);
        }
        Bukkit.getScheduler().runTaskAsynchronously(NeincraftPlugin.getInstance(), () -> savePlayerData(toSave.toArray(new PlayerData[0])));
        points.clear();
    }

    private void addPoints(UUID player, int amount){
        if(amount > 25) amount = 25;
        if(amount < 0) amount = 0;
        int currentPoints = points.getOrDefault(player, 0);
        int updatedPoints = currentPoints + amount;
        if(updatedPoints > 25) updatedPoints = 25;
        points.put(player, updatedPoints);
    }

    private void updateLastSeen(Player player){
        lastSeen.put(player.getUniqueId(), System.currentTimeMillis());
        if(afkPlayers.contains(player.getUniqueId())){
            afkPlayers.remove(player.getUniqueId());
            NeincraftUtils.formattedBroadcast(Lang.PLAYER_AFK_RETURN, MessageType.SYSTEM, "player", player.getName());
        }
    }

    public synchronized PlayerData getOrCreate(UUID uuid){
        PlayerData pd = playerStats.get(uuid);
        if(pd == null) {
            try(PlayerDataRepo repo = PlayerDataRepo.getRepository()){
                if(repo != null){
                    pd = repo.getOrCreate(uuid);
                    repo.commit();
                    playerStats.put(uuid, pd);
                }
            }catch (Exception e){
                getLogger().log(Level.WARNING, "PlayerData could not be loaded", e);
            }
        }
        if(pd != null)
            pd.setExpiryTimestamp(System.currentTimeMillis() + 300000);
        return pd;
    }

    public synchronized void savePlayerData(PlayerData... playerData){
        try(PlayerDataRepo repo = PlayerDataRepo.getRepository()){
            if(repo != null) {
                for (PlayerData pd : playerData)
                    repo.save(pd);
                repo.commit();
            }
        }catch(Exception e){
            getLogger().log(Level.WARNING, "Could not save PlayerData", e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if(!event.getPlayer().hasPlayedBefore()) {
            NeincraftUtils.formattedBroadcast(Lang.FIRST_JOIN, MessageType.SYSTEM, "player", event.getPlayer().getName());
            event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());
        }
        updateLastSeen(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        if(event.hasChangedOrientation()){
            updateLastSeen(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        lastSeen.remove(event.getPlayer().getUniqueId());
        afkPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event){
        event.renderer(chatRenderer);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void  onPlayerBuild(BlockPlaceEvent event){
        if(!event.isCancelled() && event.getPlayer().getGameMode() == GameMode.SURVIVAL)
            addPoints(event.getPlayer().getUniqueId(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void  onPlayerBreak(BlockBreakEvent event){
        if(!event.isCancelled() && event.getPlayer().getGameMode() == GameMode.SURVIVAL)
            addPoints(event.getPlayer().getUniqueId(), 1);
    }

    @Override
    public void unload() {
        if(statTask != null)
            statTask.cancel(false);
    }
}
