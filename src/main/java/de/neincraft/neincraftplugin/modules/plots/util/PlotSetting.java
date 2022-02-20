package de.neincraft.neincraftplugin.modules.plots.util;

import de.neincraft.neincraftplugin.util.Lang;

import java.util.Arrays;

public enum PlotSetting {
    SPAWN_MONSTERS(true),
    SPAWN_ANIMALS(true),
    FIRE_SPREAD(false),
    ALLOW_EXPLOSIONS(false),
    ALLOW_PVP(false, false),
    FALL_DAMAGE(true, false),
    DROWNING_DAMAGE(true, false),
    KEEP_INVENTORY(false, false),
    ALLOW_ENTER(true, false);

    private final boolean defaultValue;
    private final boolean userEditable;
    private final Lang description;

    PlotSetting(boolean defaultValue) {
        this.defaultValue = defaultValue;
        this.userEditable = true;
        description = Lang.PLOT_SETTINGS_DEFAULT_DESC;
    }

    PlotSetting(boolean defaultValue, boolean userEditable) {
        this.defaultValue = defaultValue;
        this.userEditable = userEditable;
        description = Lang.PLOT_SETTINGS_DEFAULT_DESC;
    }

    PlotSetting(boolean defaultValue, Lang description){
        this.defaultValue = defaultValue;
        this.userEditable = true;
        this.description = description;
    }

    PlotSetting(boolean defaultValue, boolean userEditable, Lang description){
        this.defaultValue = defaultValue;
        this.userEditable = userEditable;
        this.description = description;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public boolean isUserEditable() {
        return userEditable;
    }

    public Lang getDescription() {
        return description;
    }

    public static PlotSetting findByName(String name){
        return Arrays.stream(PlotSetting.values()).filter(s -> name.equalsIgnoreCase(s.name())).findFirst().orElse(null);
    }
}
