package de.neincraft.neincraftplugin.util;

import de.neincraft.neincraftplugin.modules.Module;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerLanguage;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerStats;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

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

    public static void formattedBroadcast(Lang lang, MessageType type){
        formattedBroadcast(lang, type, null);
    }

}
