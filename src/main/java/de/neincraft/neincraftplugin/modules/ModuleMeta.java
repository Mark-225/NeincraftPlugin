package de.neincraft.neincraftplugin.modules;

public class ModuleMeta {
    private NeincraftModule moduleData;
    private Module moduleInstance;

    public ModuleMeta(NeincraftModule data, Module moduleInstance){
        this.moduleData = data;
        this.moduleInstance = moduleInstance;
    }

    public NeincraftModule getModuleData() {
        return moduleData;
    }

    public Module getModuleInstance() {
        return moduleInstance;
    }
}
