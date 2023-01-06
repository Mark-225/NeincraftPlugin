package de.neincraft.neincraftplugin.modules.plots.util;

import de.neincraft.neincraftplugin.util.lang.Lang;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public enum PlotSetting {
    SPAWN_MONSTERS(true, Material.ZOMBIE_SPAWN_EGG),
    SPAWN_ANIMALS(true, Material.PIG_SPAWN_EGG),
    FIRE_SPREAD(false, Material.CAMPFIRE),
    ALLOW_EXPLOSIONS(false, Material.TNT),
    ALLOW_PVP(false, Material.IRON_SWORD),
    FALL_DAMAGE(true, false, Material.FEATHER),
    DROWNING_DAMAGE(true, false, Material.WATER_BUCKET),
    KEEP_INVENTORY(false, false, Material.CHEST),
    ALLOW_ENTER(true, false, Material.IRON_DOOR);

    private final boolean defaultValue;
    private final boolean userEditable;

    private final ItemStack displayItem;

    PlotSetting(boolean defaultValue) {
        this(defaultValue, true);
    }

    PlotSetting(boolean defaultValue, Material displayItem) {
        this(defaultValue, true, displayItem);
    }

    PlotSetting(boolean defaultValue, ItemStack displayItem) {
        this(defaultValue, true, displayItem);
    }

    PlotSetting(boolean defaultValue, boolean userEditable) {
        this(defaultValue, userEditable, Material.STONE);
    }

    PlotSetting(boolean defaultValue, boolean userEditable, Material displayMaterial) {
        this(defaultValue, userEditable, new ItemStack(displayMaterial));
    }

    PlotSetting(boolean defaultValue, boolean userEditable, ItemStack displayItem) {
        this.defaultValue = defaultValue;
        this.userEditable = userEditable;
        this.displayItem = displayItem;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public boolean isUserEditable() {
        return userEditable;
    }

    public Lang getDescription() {
        try{
            return Lang.valueOf("PLOT_SETTINGS_" + name() + "_DESC");
        } catch (IllegalArgumentException e) {
            return Lang.PLOT_SETTINGS_DEFAULT_DESC;
        }
    }

    public Lang getDisplayName(){
        try{
            return Lang.valueOf("PLOT_SETTINGS_" + name() + "_NAME");
        } catch (IllegalArgumentException e) {
            return Lang.PLOT_SETTINGS_DEFAULT_NAME;
        }
    }

    public ItemStack getDisplayItem() {
        return displayItem.clone();
    }

    public static PlotSetting findByName(String name){
        return Arrays.stream(PlotSetting.values()).filter(s -> name.equalsIgnoreCase(s.name())).findFirst().orElse(null);
    }
}
