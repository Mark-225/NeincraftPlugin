package de.neincraft.neincraftplugin.modules;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModuleLoader {
    private Map<String, ModuleMeta> modules = new HashMap<>();

    private List<ModuleMeta> instantiate(){
        Reflections reflections = NeincraftUtils.buildReflections();
        Set<Class<? extends AbstractModule>> classes = reflections.getSubTypesOf(AbstractModule.class);

        List<ModuleMeta> metaList = new ArrayList<>();

        for(Class<? extends AbstractModule> current : classes){
            NeincraftModule moduleData = current.getAnnotation(NeincraftModule.class);
            if(moduleData == null){
                NeincraftPlugin.getInstance().getLogger().log(Level.WARNING, String.format("Could not load Module meta of \"%s\". Missing NeincraftModule annotation!", current.getName()));
                continue;
            }
            AbstractModule moduleInstance;
            try {
                moduleInstance = current.getDeclaredConstructor(null).newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                NeincraftPlugin.getInstance().getLogger().log(Level.WARNING, String.format("Could not instantiate class \"%s\" for Module \"%s\". See stacktrace for more details.", current.getName(), moduleData.id()));
                e.printStackTrace();
                continue;
            }
            metaList.add(new ModuleMeta(moduleData, moduleInstance));
        }
        return metaList;
    }

    /**
     * Loads all Modules and initializes them according to their dependencies.
     * @return true, if no vital modules failed to load
     */
    public boolean loadModules(){
        modules.clear();
        List<ModuleMeta> availableModules = instantiate();
        List<ModuleMeta> failedModules = new ArrayList<>();
        List<ModuleMeta> toRemove;
        List<ModuleMeta> loadOrder = new ArrayList<>();
        do{
            toRemove = new ArrayList<>();
            for(ModuleMeta mm : availableModules){
                if(mm.getModuleData().requiredModules().length == 0 || modules.keySet().containsAll(Arrays.asList(mm.getModuleData().requiredModules()))){
                    NeincraftPlugin.getInstance().getLogger().log(Level.INFO, String.format("Loading Module \"%s\"", mm.getModuleData().id()));
                    modules.put(mm.getModuleData().id(), mm);
                    loadOrder.add(mm);
                    toRemove.add(mm);
                }
            }
            availableModules.removeAll(toRemove);
        }while(toRemove.size() > 0);

        loadOrder.forEach(meta -> {
            meta.getModuleInstance().preInit();
        });
        loadOrder.forEach(meta ->{
            boolean success = meta.getModuleInstance().initialize();
            if(!success){
                NeincraftPlugin.getInstance().getLogger().log(Level.WARNING, String.format("Module \"%s\" failed to initialise.", meta.getModuleData().id()));
                failedModules.add(meta);
            }
        });
        loadOrder.forEach(meta -> {
            if(!failedModules.contains(meta))
                meta.getModuleInstance().postInit();
        });

        if(availableModules.size() > 0)
            NeincraftPlugin.getInstance().getLogger().log(Level.WARNING, String.format("The Modules [%s] could not be loaded due to unresolvable dependencies.", availableModules.stream().map(mm -> mm.getModuleData().id()).collect(Collectors.joining(","))));
        if(Stream.concat(availableModules.stream(), failedModules.stream()).anyMatch(mm -> mm.getModuleData().isVital())){
            return false;
        }
        NeincraftPlugin.getInstance().getLogger().log(Level.INFO, String.format("Module initialization complete. Loaded modules: [%s]", modules.keySet().stream().collect(Collectors.joining(","))));
        return true;
    }

    public void unloadModules(){
        List<String> toRemove;
        do{
            toRemove = new ArrayList<>();
            for(ModuleMeta cur : new ArrayList<>(modules.values())){
                if(modules.values().stream().allMatch(mm -> !Arrays.asList(mm.getModuleData().requiredModules()).contains(cur.getModuleData().id()))){
                    cur.getModuleInstance().unload();
                    toRemove.add(cur.getModuleData().id());
                }
            }
            for(String key : toRemove){
                modules.remove(key);
            }
        }while (!toRemove.isEmpty());
        if(!modules.isEmpty())
            NeincraftPlugin.getInstance().getLogger().log(Level.WARNING, String.format("Could not unload modules [%s]", modules.keySet().stream().collect(Collectors.joining(","))));
    }
}
