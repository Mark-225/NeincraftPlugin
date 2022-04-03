package de.neincraft.neincraftplugin.modules.discord;

import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.util.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DiscordCommand implements CommandExecutor, TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Lang.WRONG_EXECUTOR.getComponent(null));
            return true;
        }
        Player player = (Player) sender;
        if(args.length == 0) {
            player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(player).replace(
                    "label", label,
                    "args", "[link|unlink|opt-out|opt-in]"
            ).toComponent());
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "link" -> {
                if(args.length != 2){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(player).replace(
                            "label", label,
                            "args", "link <token>"
                    ).toComponent());
                    break;
                }
                AbstractModule.getInstance(DiscordModule.class).ifPresentOrElse(
                        discordModule -> {
                            boolean success = discordModule.linkAccount(player.getUniqueId(), args[1]);
                            if(success) {
                                player.sendMessage(Lang.DISCORD_LINK_SUCCESS.getMinedown(player).toComponent());
                            }else{
                                player.sendMessage(Lang.DISCORD_LINK_FAIL.getMinedown(player).toComponent());
                            }
                            },
                        () -> {
                            player.sendMessage(Lang.FATAL_ERROR.getComponent(player));
                        }

                );
            }
            case "unlink" -> {
                if(args.length != 1){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(player).replace(
                            "label", label,
                            "args", args[0]
                    ).toComponent());
                    break;
                }
                AbstractModule.getInstance(DiscordModule.class).ifPresentOrElse(
                        discordModule -> {
                            discordModule.unlinkAccount(player.getUniqueId());
                            player.sendMessage(Lang.DISCORD_UNLINK_SUCCESS.getMinedown(player).toComponent());
                        },
                        () -> {
                            player.sendMessage(Lang.FATAL_ERROR.getComponent(player));
                    });
            }
            case "opt-out" -> {
                if(args.length != 1){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(player).replace(
                            "label", label,
                            "args", args[0]
                    ).toComponent());
                    break;
                }
                player.getPersistentDataContainer().set(DiscordModule.DISCORD_OPT_OUT, PersistentDataType.BYTE, (byte) 1);
                player.sendMessage(Lang.DISCORD_OPT_OUT_SUCCESS.getMinedown(player).toComponent());
            }
            case "opt-in" -> {
                if(args.length != 1){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(player).replace(
                            "label", label,
                            "args", args[0]
                    ).toComponent());
                    break;
                }
                player.getPersistentDataContainer().remove(DiscordModule.DISCORD_OPT_OUT);
                player.sendMessage(Lang.DISCORD_OPT_IN_SUCCESS.getMinedown(player).toComponent());
            }
            case "sendverificationmsg" -> {
                if(!player.hasPermission("neincraft.discord.sendVerificationMsg")) break;
                if(args.length != 1){
                    player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(player).replace(
                            "label", label,
                            "args", args[0]
                    ).toComponent());
                    break;
                }
                AbstractModule.getInstance(DiscordModule.class).ifPresentOrElse(
                        DiscordModule::sendVerificationMessage,
                        () -> {
                            player.sendMessage(Lang.FATAL_ERROR.getComponent(player));
                        }
                );
            }
        }
        return true;
    }
}
