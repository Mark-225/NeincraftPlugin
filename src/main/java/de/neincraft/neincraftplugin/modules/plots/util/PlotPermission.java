package de.neincraft.neincraftplugin.modules.plots.util;

public enum PlotPermission {

    BUILD(PermissionValue.ALLOW, PermissionValue.ALLOW, PermissionValue.DENY);

    private PermissionValue ownerDefault;
    private PermissionValue memberDefault;
    private PermissionValue publicDefault;

    PlotPermission(PermissionValue ownerDefault, PermissionValue memberDefault, PermissionValue publicDefault) {
        this.ownerDefault = ownerDefault;
        this.memberDefault = memberDefault;
        this.publicDefault = publicDefault;
    }

    public PermissionValue getOwnerDefault() {
        return ownerDefault;
    }

    public PermissionValue getMemberDefault() {
        return memberDefault;
    }

    public PermissionValue getPublicDefault() {
        return publicDefault;
    }
}
