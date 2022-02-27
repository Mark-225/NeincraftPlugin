package de.neincraft.neincraftplugin.modules;

public class ModuleMeta {
    private NeincraftModule moduleData;
    private AbstractModule moduleInstance;

    public ModuleMeta(NeincraftModule data, AbstractModule moduleInstance){
        this.moduleData = data;
        this.moduleInstance = moduleInstance;
    }

    public NeincraftModule getModuleData() {
        return moduleData;
    }

    public AbstractModule getModuleInstance() {
        return moduleInstance;
    }
}
