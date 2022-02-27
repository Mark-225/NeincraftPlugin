package de.neincraft.neincraftplugin.modules.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface SimpleTabCompleter extends TabCompleter {

    default List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String string, @NotNull String[] strings){
        List<String> completions = new ArrayList<>();
        tabComplete(commandSender, command, string, strings, completions);
        completions.removeIf(s -> !s.toLowerCase().startsWith(strings[strings.length-1].toLowerCase()));
        return completions;
    }

    void tabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args, List<String> completions);

}
