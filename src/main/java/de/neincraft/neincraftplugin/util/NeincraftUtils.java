package de.neincraft.neincraftplugin.util;

import de.neincraft.neincraftplugin.modules.Module;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerLanguage;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.Scanners;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class NeincraftUtils {

    public static Reflections buildReflections(){
        return new Reflections(new ConfigurationBuilder()
                .forPackage("de.neincraft.neincraftplugin.modules")
                .setScanners(Scanners.SubTypes, Scanners.Resources, Scanners.TypesAnnotated));
    }

    public static String uuidToName(UUID uuid){
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return op.getName();
    }

    public static Optional<UUID> nameToUuid(String name){
        OfflinePlayer op = Bukkit.getOfflinePlayerIfCached(name);
        return op != null ? Optional.of(op.getUniqueId()) : Optional.empty();
    }

    public static PlayerLanguage getPlayerLanguage(Player p){
        if(p == null) return PlayerLanguage.AUTO;
        Optional<PlayerStats> playerStats = Module.getInstance(PlayerStats.class);
        if(playerStats.isEmpty()) return PlayerLanguage.AUTO;
        return playerStats.get().getOrCreate(p.getUniqueId()).getLanguage();
    }

}
