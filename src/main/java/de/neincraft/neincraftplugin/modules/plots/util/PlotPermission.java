package de.neincraft.neincraftplugin.modules.plots.util;

import java.util.Arrays;

public enum PlotPermission {

    BUILD(true, true, false, "build"),
    TELEPORT_HOME(true, true, false, "home"),
    USE_WOOD_BUTTONS(true, true, true, "wood_buttons"),
    USE_STONE_BUTTONS(true, true, false, "stone_buttons"),
    USE_LEVERS(true, true, false, "levers"),
    OPEN_CONTAINERS(true, true, false, "containers"),
    USE_RIDEABLES(true, true, false, "rideables"),
    PLACE_RIDEABLES(true, true, false, "place_rideables"),
    THROW_PROJECTILES(true, true, false, "projectiles"),
    FIRE_ARROWS(true, true, false, "arrows"),
    TRADE_VILLAGERS(true, true, false, "trade_villagers"),
    SHEAR_ENTITIES(true, true, false, "shear_entities"),
    SADDLE_ENTITIES(true, true, false, "saddle_entities"),
    HURT_NON_MONSTERS(true, true, false, "hurt_non_monsters"),
    USE_WOODEN_DOORS(true, true, true, "wooden_doors"),
    USE_OTHER_OPENABLES(true, true, false, "openables"),
    USE_ANVILS(true, true, false, "anvils"),
    USE_ENDERCHESTS(true, true, true, "enderchests"),
    USE_CRAFTING_TABLE(true, true, true, "crafting_table"),
    USE_REDSTONE_COMPONENTS(true, true, false, "redstone_components"),
    USE_LEADS(true, true, false, "leads"),
    USE_JUKEBOX(true, true, false, "jukebox"),
    READ_LECTERN(true, true, true, "read_lectern"),
    EDIT_LECTERN(true, true, false, "edit_lectern"),
    HARVEST_HIVES(true, true, false, "harvest_hives");

    private boolean ownerDefault;
    private boolean memberDefault;
    private boolean publicDefault;
    private String bypassPermission;

    PlotPermission(boolean ownerDefault, boolean memberDefault, boolean publicDefault) {
        this.ownerDefault = ownerDefault;
        this.memberDefault = memberDefault;
        this.publicDefault = publicDefault;
        this.bypassPermission = "other";
    }

    PlotPermission(boolean ownerDefault, boolean memberDefault, boolean publicDefault, String bypassPermission) {
        this.ownerDefault = ownerDefault;
        this.memberDefault = memberDefault;
        this.publicDefault = publicDefault;
        this.bypassPermission = bypassPermission;
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
}
