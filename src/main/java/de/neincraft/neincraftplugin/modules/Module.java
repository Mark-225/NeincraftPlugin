package de.neincraft.neincraftplugin.modules;

import de.neincraft.neincraftplugin.NeincraftPlugin;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class Module {

    private static ConcurrentHashMap<Class<? extends Module>, Module> instances = new ConcurrentHashMap<>();

    protected static void clearModuleRegistry(){
        instances.clear();
    }

    protected static Logger getLogger(){
        return NeincraftPlugin.getInstance().getLogger();
    }

    public Module(){
        if(!instances.containsKey(this.getClass()))
            instances.put(this.getClass(), this);
    }

    @Nonnull
    public static <T extends Module> Optional<T> getInstance(Class<T> moduleClass){
        Module m = instances.get(moduleClass);
        if(m == null || !moduleClass.isAssignableFrom(m.getClass()))
            return Optional.empty();
        @SuppressWarnings("unchecked")
        T t = (T) m;
        return Optional.of(t);
    }

    public static Map<Class<? extends Module>, Module> getInstances(){
        return instances;
    }

    public boolean initialize(){
        try {
            return initModule();
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    protected abstract boolean initModule();

    public abstract void unload();

    public void preInit() {}

    public void postInit(){}

}
