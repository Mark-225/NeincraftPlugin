package de.neincraft.neincraftplugin.modules.plots.util;

import de.neincraft.neincraftplugin.util.lang.Lang;
import dev.dbassett.skullcreator.SkullCreator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public enum PlotPermission {

    BUILD(true, true, false, "build", Material.OAK_WOOD),
    TELEPORT_HOME(true, true, false, "home", SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzVhMzViNWNhMTUyNjg2ODVjNDY2MDUzNWU1ODgzZDIxYTVlYzU3YzU1ZDM5NzIzNDI2OWFjYjVkYzI5NTRmIn19fQ==")),
    USE_WOOD_BUTTONS(true, true, true, "wood_buttons", Material.OAK_BUTTON),
    USE_STONE_BUTTONS(true, true, false, "stone_buttons", Material.STONE_BUTTON),
    USE_LEVERS(true, true, false, "levers", Material.LEVER),
    OPEN_CONTAINERS(true, true, false, "containers", Material.CHEST),
    USE_RIDEABLES(true, true, false, "rideables", Material.CARROT_ON_A_STICK),
    PLACE_RIDEABLES(true, true, false, "place_rideables", Material.MINECART),
    THROW_PROJECTILES(true, true, false, "projectiles", Material.SNOWBALL),
    FIRE_ARROWS(true, true, false, "arrows", Material.ARROW),
    TRADE_VILLAGERS(true, true, false, "trade_villagers", Material.EMERALD),
    SHEAR_ENTITIES(true, true, false, "shear_entities", Material.SHEARS),
    SADDLE_ENTITIES(true, true, false, "saddle_entities", Material.SADDLE),
    HURT_NON_MONSTERS(true, true, false, "hurt_non_monsters", Material.IRON_AXE),
    USE_WOODEN_DOORS(true, true, true, "wooden_doors", Material.OAK_DOOR),
    USE_OTHER_OPENABLES(true, true, false, "openables", Material.OAK_TRAPDOOR),
    USE_ANVILS(true, true, false, "anvils", Material.ANVIL),
    USE_ENDERCHESTS(true, true, true, "enderchests", Material.ENDER_CHEST),
    USE_CRAFTING_TABLE(true, true, true, "crafting_table", Material.CRAFTING_TABLE),
    USE_REDSTONE_COMPONENTS(true, true, false, "redstone_components", Material.REDSTONE),
    USE_LEADS(true, true, false, "leads", Material.LEAD),
    USE_JUKEBOX(true, true, false, "jukebox", Material.JUKEBOX),
    READ_LECTERN(true, true, true, "read_lectern", Material.LECTERN),
    EDIT_LECTERN(true, true, false, "edit_lectern", Material.WRITABLE_BOOK),
    HARVEST_HIVES(true, true, false, "harvest_hives", Material.HONEYCOMB);

    private boolean ownerDefault;
    private boolean memberDefault;
    private boolean publicDefault;
    private String bypassPermission;

    private ItemStack displayItem;

    PlotPermission(boolean ownerDefault, boolean memberDefault, boolean publicDefault) {
        this(ownerDefault, memberDefault, publicDefault, "other");
    }

    PlotPermission(boolean ownerDefault, boolean memberDefault, boolean publicDefault, String bypassPermission, Material displayItem) {
        this(ownerDefault, memberDefault, publicDefault, bypassPermission, new ItemStack(displayItem));
    }

    PlotPermission(boolean ownerDefault, boolean memberDefault, boolean publicDefault, String bypassPermission) {
        this(ownerDefault, memberDefault, publicDefault, bypassPermission, SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTBhNzk4OWI1ZDZlNjIxYTEyMWVlZGFlNmY0NzZkMzUxOTNjOTdjMWE3Y2I4ZWNkNDM2MjJhNDg1ZGMyZTkxMiJ9fX0="));
    }

    PlotPermission(boolean ownerDefault, boolean memberDefault, boolean publicDefault, String bypassPermission, ItemStack displayItem) {
        this.ownerDefault = ownerDefault;
        this.memberDefault = memberDefault;
        this.publicDefault = publicDefault;
        this.bypassPermission = bypassPermission;
        this.displayItem = displayItem;
    }

    public boolean getOwnerDefault() {
        return ownerDefault;
    }

    public boolean getMemberDefault() {
        return memberDefault;
    }

    public boolean getPublicDefault() {
        return publicDefault;
    }

    public String getBypassPermission() {
        return bypassPermission;
    }

    public static PlotPermission findByName(String name){
        return Arrays.stream(PlotPermission.values()).filter(permission -> permission.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public ItemStack getDisplayItemCopy() {
        return displayItem.clone();
    }

    public Lang getDisplayName() {
        try {
            return Lang.valueOf("PLOT_PERMISSION_" + name() + "_NAME");
        } catch (IllegalArgumentException e) {
            return Lang.PLOT_PERMISSION_DEFAULT_NAME;
        }
    }

    public Lang getDescription(){
        try {
            return Lang.valueOf("PLOT_PERMISSION_" + name() + "_DESCRIPTION");
        } catch (IllegalArgumentException e) {
            return Lang.PLOT_PERMISSION_DEFAULT_DESCRIPTION;
        }
    }
}
