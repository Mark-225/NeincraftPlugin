package de.neincraft.neincraftplugin.modules.plots.util;

import java.util.Arrays;

public enum PlotPermission {

    BUILD(true, true, false),
    TELEPORT_HOME(true, true, false),
    USE_WOOD_BUTTONS(true, true, true),
    USE_STONE_BUTTONS(true, true, false),
    USE_LEVERS(true, true, false),
    OPEN_CONTAINERS(true, true, false),
    USE_RIDEABLES(true, true, false),
    PLACE_RIDEABLES(true, true, false),
    THROW_PROJECTILES(true, true, false),
    FIRE_ARROWS(true, true, false),
    INTERACT_WITH_ENTITIES(true, true, false),
    HURT_NON_MONSTERS(true, true, false),
    USE_WOODEN_DOORS(true, true, true),
    USE_OTHER_OPENABLES(true, true, false),
    USE_ANVILS(true, true, false),
    USE_ENDERCHESTS(true, true, true),
    USE_CRAFTING_TABLE(true, true, true),
    USE_REDSTONE_COMPONENTS(true, true, false),
    USE_LEADS(true, true, false),
    USE_JUKEBOX(true, true, false),
    READ_LECTERN(true, true, true),
    EDIT_LECTERN(true, true, false),
    HARVEST_HIVES(true, true, false);

    private boolean ownerDefault;
    private boolean memberDefault;
    private boolean publicDefault;

    PlotPermission(boolean ownerDefault, boolean memberDefault, boolean publicDefault) {
        this.ownerDefault = ownerDefault;
        this.memberDefault = memberDefault;
        this.publicDefault = publicDefault;
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

    public static PlotPermission findByName(String name){
        return Arrays.stream(PlotPermission.values()).filter(permission -> permission.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
