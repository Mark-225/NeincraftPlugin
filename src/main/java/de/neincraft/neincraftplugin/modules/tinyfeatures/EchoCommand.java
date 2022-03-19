package de.neincraft.neincraftplugin.modules.tinyfeatures;

import de.themoep.minedown.adventure.MineDown;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EchoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        MineDown mineDown = new MineDown(Arrays.stream(args).collect(Collectors.joining(" ")));
        try {
            sender.sendMessage(mineDown.toComponent());
        }catch(Exception e){
            //Sometimes, MineDown throws errors while parsing, but we don't care about those
        }
        return true;
    }
}
