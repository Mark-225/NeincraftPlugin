package de.neincraft.neincraftplugin.modules.portals;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.commands.SimpleTabCompleter;
import de.neincraft.neincraftplugin.modules.portals.dto.Portal;
import de.neincraft.neincraftplugin.util.lang.Lang;
import de.themoep.minedown.adventure.MineDown;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class PortalCommand implements CommandExecutor, SimpleTabCompleter {

    public final NamespacedKey portalEditName = new NamespacedKey(NeincraftPlugin.getInstance(), "edit-portal");

    @Override
    public void tabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args, List<String> completions) {
        if(args.length == 1){
            completions.addAll(List.of("create", "edit", "delete"));
        }else if (args.length == 2 && args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("delete")){
            AbstractModule.getInstance(PortalModule.class).ifPresent(pm -> {
                completions.addAll(pm.getLoadedPortalNames());
            });
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(Lang.WRONG_EXECUTOR.getComponent(null));
            return true;
        }
        Player player = (Player) sender;

        if(args.length != 2){
            player.sendMessage(Lang.WRONG_SYNTAX.getMinedown(player).replace(
                    "label", label,
                    "args", "[create|edit|delete] [name]"
            ).toComponent());
            return true;
        }
        Optional<PortalModule> oModule = AbstractModule.getInstance(PortalModule.class);
        if(oModule.isEmpty()){
            player.sendMessage(Lang.FATAL_ERROR.getComponent(player));
            return true;
        }
        PortalModule pm = oModule.get();
        switch (args[0].toLowerCase()){
            case "create" -> {
                String name = args[1];
                if(pm.getPortal(name) != null){
                    player.sendMessage(Lang.PORTAL_ALREADY_EXISTS.getComponent(player));
                    break;
                }
                pm.createPortal(args[1]);
                player.sendMessage(Lang.PORTAL_CREATED.getMinedown(player).replace(
                        "portal", args[1].toLowerCase()
                ).toComponent());
            }
            case "edit" -> {
                Portal portal = pm.getPortal(args[1]);
                if(portal == null){
                    player.sendMessage(Lang.PORTAL_NOT_FOUND.getComponent(player));
                    break;
                }
                if(!player.getInventory().addItem(createPortalEditTool(args[1].toLowerCase())).isEmpty()){
                    player.sendMessage(Lang.INVENTORY_FULL.getComponent(player));
                    break;
                }
                player.sendMessage(Lang.PORTAL_BEGIN_EDIT.getMinedown(player).replace(
                        "portal", args[1].toLowerCase()
                ).toComponent());
            }
            case "delete" -> {
                Portal portal = pm.getPortal(args[1]);
                if(portal == null){
                    player.sendMessage(Lang.PORTAL_NOT_FOUND.getComponent(player));
                    break;
                }
                pm.remove(portal.getName());
                player.sendMessage(Lang.PORTAL_DELETED.getComponent(player));
            }
        }
        return true;
    }

    private ItemStack createPortalEditTool(String name){
        ItemStack is = new ItemStack(Material.STICK);
        is.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        is.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        ItemMeta im = is.getItemMeta();
        im.displayName(new MineDown("Portal Tool - " + name).toComponent());
        im.getPersistentDataContainer().set(portalEditName, PersistentDataType.STRING, name);
        is.setItemMeta(im);
        return is;
    }
}
