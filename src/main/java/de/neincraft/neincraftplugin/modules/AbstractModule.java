package de.neincraft.neincraftplugin.modules;

import de.neincraft.neincraftplugin.NeincraftPlugin;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class AbstractModule {

    private static ConcurrentHashMap<Class<? extends AbstractModule>, AbstractModule> instances = new ConcurrentHashMap<>();

    protected static void clearModuleRegistry(){
        instances.clear();
    }

    protected static Logger getLogger(){
        return NeincraftPlugin.getInstance().getLogger();
    }

    public AbstractModule(){
        if(!instances.containsKey(this.getClass()))
            instances.put(this.getClass(), this);
    }

    @Nonnull
    public static <T extends AbstractModule> Optional<T> getInstance(Class<T> moduleClass){
        AbstractModule m = instances.get(moduleClass);
        if(m == null || !moduleClass.isAssignableFrom(m.getClass()))
            return Optional.empty();
        @SuppressWarnings("unchecked")
        T t = (T) m;
        return Optional.of(t);
    }

    public static Map<Class<? extends AbstractModule>, AbstractModule> getInstances(){
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
