package de.neincraft.neincraftplugin.modules.misc.commands;

import de.neincraft.neincraftplugin.modules.commands.SimpleTabCompleter;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerLanguage;
import de.neincraft.neincraftplugin.util.lang.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class TestLang implements CommandExecutor, SimpleTabCompleter {
    @Override
    public void tabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args, List<String> completions) {
        if(args.length == 1){
            completions.addAll(Arrays.stream(Lang.values()).map(val -> val.name().toLowerCase()).toList());
        }else if(args.length == 2){
            completions.addAll(Arrays.stream(PlayerLanguage.values()).map(val -> val.name().toLowerCase()).toList());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 1 || args.length > 2){
            sender.sendMessage(Lang.WRONG_SYNTAX.getMinedown(sender instanceof Player player ? player : null).replace(
                    "label", label,
                    "args", "[lang_id] [language]"
            ).toComponent());
            return true;
        }
        Lang lang = Arrays.stream(Lang.values()).filter(val -> val.name().equalsIgnoreCase(args[0])).findAny().orElse(null);
        PlayerLanguage language = PlayerLanguage.AUTO;
        if(args.length == 2)
            language = Arrays.stream(PlayerLanguage.values()).filter(val -> val.name().equalsIgnoreCase(args[1])).findAny().orElse(null);
        if(lang != null && language != null)
            sender.sendMessage(lang.getComponent(language, sender instanceof Player player ? player : null));
        return true;
    }
}
