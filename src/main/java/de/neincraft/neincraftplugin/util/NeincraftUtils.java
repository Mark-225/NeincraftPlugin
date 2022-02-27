package de.neincraft.neincraftplugin.util;

import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerLanguage;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerStats;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.*;

public abstract class NeincraftUtils {

    public static Reflections buildReflections(){
        return new Reflections(new ConfigurationBuilder()
                .forPackage("de.neincraft.neincraftplugin.modules")
                .setScanners(Scanners.SubTypes, Scanners.Resources, Scanners.TypesAnnotated));
    }

    public static String uuidToName(UUID uuid){
        if(uuid == null) return "Server";
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return op.getName();
    }

    public static Optional<UUID> nameToUuid(String name){
        OfflinePlayer op = Bukkit.getOfflinePlayerIfCached(name);
        return op != null ? Optional.of(op.getUniqueId()) : Optional.empty();
    }

    public static PlayerLanguage getPlayerLanguage(Player p){
        if(p == null) return PlayerLanguage.AUTO;
        Optional<PlayerStats> playerStats = AbstractModule.getInstance(PlayerStats.class);
        if(playerStats.isEmpty()) return PlayerLanguage.AUTO;
        return playerStats.get().getOrCreate(p.getUniqueId()).getLanguage();
    }

    public static PlayerLanguage resolvePlayerLanguage(Player p){
        PlayerLanguage lang = getPlayerLanguage(p);
        if(lang == PlayerLanguage.AUTO)
            lang = PlayerLanguage.fromLocale(p.locale());
        return lang;
    }

    public static void formattedBroadcast(Lang lang, MessageType type, @Nullable String... replacements){
        for (Player p : Bukkit.getOnlinePlayers()){
            MineDown minedown = lang.getMinedown(p);
            if(replacements != null){
                minedown.replace(replacements);
            }
            p.sendMessage(minedown.toComponent(), type);
        }
        MineDown mineDown = lang.getMinedown(PlayerLanguage.ENGLISH, null);
        if(replacements != null){
            mineDown.replace(replacements);
        }
        Bukkit.getConsoleSender().sendMessage(mineDown.toComponent(), type);
    }

    public static String createProgressBar(String borderColor, String completeColor, String incompleteColor, int length, double progress){
        if(progress < 0) progress = 0;
        if(progress > 1) progress = 1;
        int complete = (int) Math.round(progress * (double) length);
        int incomplete = length - complete;
        return "[\\[](" + borderColor + ")[" + repeatString("\u2588", complete) +  "](" + completeColor +")[" + repeatString("\u2588", incomplete) + "](" + incompleteColor + ")[\\]](" + borderColor + ")";
    }

    private static String repeatString(String str, int amount){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < amount; i++){
            sb.append(str);
        }
        return sb.toString();
    }

    public static void formattedBroadcast(Lang lang, MessageType type){
        formattedBroadcast(lang, type, null);
    }

    public static void teleportToLocation(Player p, Location loc){
        teleportToLocation(p, loc, true);
    }

    public static void teleportToLocation(Player p, Location loc, boolean bringLeashedEntities){
        List<LivingEntity> toTeleport = Collections.emptyList();
        if(bringLeashedEntities){
            toTeleport = p.getWorld().getNearbyEntitiesByType(LivingEntity.class, p.getLocation(), 8).stream().filter(le -> le.isLeashed() && le.getLeashHolder() == p).toList();
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, 1);
        if(p.teleport(loc)){
            p.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            toTeleport.forEach(le -> le.teleport(loc));
        }
    }

}
