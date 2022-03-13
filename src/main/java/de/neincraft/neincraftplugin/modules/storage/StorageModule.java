package de.neincraft.neincraftplugin.modules.storage;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.commands.InjectCommand;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.function.Consumer;

@NeincraftModule(id = "Storage", requiredModules = {"database"})
public class StorageModule extends AbstractModule {
    private final HashMap<UUID, List<Consumer<Storage>>> currentlyLoading = new HashMap<>();
    private final HashMap<UUID, Storage> loadedPlayerStorages = new HashMap<>();

    @InjectCommand("storage")
    private StorageCommand storageCommand;

    @Override
    protected boolean initModule() {
        Bukkit.getScheduler().runTaskTimer(NeincraftPlugin.getInstance(), () -> {
            long time = System.currentTimeMillis();
            loadedPlayerStorages.values().removeIf(storage -> storage.getOpenInterfaces().isEmpty() && time - storage.getLastOpened() > 1_200_000);
        }, 12_000, 12_000);
        return true;
    }

    public boolean requestStorage(UUID uuid, Consumer<Storage> syncCallback){
        if(loadedPlayerStorages.containsKey(uuid)) {
            syncCallback.accept(loadedPlayerStorages.get(uuid));
            return true;
        }
        if(currentlyLoading.containsKey(uuid)){
            currentlyLoading.get(uuid).add(syncCallback);
            return false;
        }
        currentlyLoading.put(uuid, new ArrayList<>(List.of(syncCallback)));
        Bukkit.getScheduler().runTaskAsynchronously(NeincraftPlugin.getInstance(), () -> {
           Storage storage = Storage.loadFromUuid(uuid);
           Bukkit.getScheduler().runTask(NeincraftPlugin.getInstance(), () ->{
               loadedPlayerStorages.put(uuid, storage);
               if(currentlyLoading.containsKey(uuid))
                   currentlyLoading.get(uuid).forEach(c -> c.accept(storage));
               currentlyLoading.remove(uuid);
           });
        });
        return false;
    }

    public Optional<Storage> getStorageImmediately(UUID uuid){
        return Optional.ofNullable(loadedPlayerStorages.get(uuid));
    }


    @Override
    public void unload() {
        if(!loadedPlayerStorages.isEmpty())
            loadedPlayerStorages.values().forEach(Storage::save);
    }
}
