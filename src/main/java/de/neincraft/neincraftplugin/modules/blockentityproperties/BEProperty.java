package de.neincraft.neincraftplugin.modules.blockentityproperties;

import de.neincraft.neincraftplugin.util.Lang;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public enum BEProperty {

    INFDISP(ts -> ts instanceof Dispenser || ts instanceof Dropper,"admin.infdisp", (player) ->{
        ItemStack is = new ItemStack(Material.DISPENSER);
        ItemMeta im = is.getItemMeta();
        im.displayName(Lang.BE_NAME_INFDISP.getComponent(player));
        im.lore(List.of(Lang.BE_DESC_INFDISP.getComponent(player)));
        is.setItemMeta(im);
        return is;
    }),
    PUBLIC(ts -> ts instanceof Container container,"user.public", (player) -> {
        ItemStack is = new ItemStack(Material.CHEST);
        ItemMeta im = is.getItemMeta();
        im.displayName(Lang.BE_NAME_PUBLIC.getComponent(player));
        im.lore(List.of(Lang.BE_DESC_PUBLIC.getComponent(player)));
        is.setItemMeta(im);
        return is;
    });

    private final Predicate<TileState> targetPredicate;
    private final String requiredPermission;
    private final Function<Player, ItemStack> displayItem;

    BEProperty(Predicate<TileState> targetPredicate, String requiredPermission, Function<Player, ItemStack> displayItem) {
        this.targetPredicate = targetPredicate;
        this.requiredPermission = requiredPermission;
        this.displayItem = displayItem;
    }

    public Predicate<TileState> getTargetPredicate() {
        return targetPredicate;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public Function<Player, ItemStack> getDisplayItem() {
        return displayItem;
    }

    public String getKeyIdentifier(){
        return "bep-" + name().toLowerCase();
    }
}
