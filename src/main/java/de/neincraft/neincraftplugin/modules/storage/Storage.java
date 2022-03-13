package de.neincraft.neincraftplugin.modules.storage;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerStats;
import de.neincraft.neincraftplugin.modules.storage.dto.StorageItem;
import de.neincraft.neincraftplugin.modules.storage.dto.StorageSystem;
import de.neincraft.neincraftplugin.modules.storage.repository.StorageRepository;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

public class Storage {

    private static final List<CreativeCategory> categoryOrder = List.of(CreativeCategory.BUILDING_BLOCKS, CreativeCategory.FOOD, CreativeCategory.TOOLS, CreativeCategory.COMBAT, CreativeCategory.DECORATIONS, CreativeCategory.REDSTONE, CreativeCategory.BREWING, CreativeCategory.MISC, CreativeCategory.TRANSPORTATION);

    private int getIndex(CreativeCategory category){
        return categoryOrder.contains(category) ? categoryOrder.indexOf(category) : categoryOrder.size();
    }

    private int compareMaterials(Material m1, Material m2){
        int categoryResult = getIndex(m1.getCreativeCategory()) - getIndex(m2.getCreativeCategory());
        if(categoryResult != 0) return categoryResult;
        return m1.name().compareToIgnoreCase(m2.name());
    }

    private int compareItems(ItemStack i1, ItemStack i2){

        return compareMaterials(i1.getType(), i2.getType());
    }

    private final StorageSystem storageData;

    private Map<ItemStack, StorageItem> itemMappings;
    private long lastOpened;
    private final List<StorageInterface> openInterfaces = new ArrayList<>();

    public static Storage loadFromUuid(UUID uuid){
        try(StorageRepository repository = StorageRepository.getRepository()){
            if(repository == null) return null;
            StorageSystem storageData = repository.getOrCreate(uuid);
            if(storageData.getStorageId() != 0) return new Storage(storageData);
            repository.save(storageData);
            repository.commit();
            return new Storage(repository.getOrCreate(uuid));
        }catch(Exception e){
            NeincraftUtils.getLogger().log(Level.WARNING, "Could not load Storage", e);
        }
        return null;
    }

    private Storage(StorageSystem storageData){
        this.storageData = storageData;
        itemMappings = updateSerializedData(storageData.getItems());
        lastOpened = System.currentTimeMillis();
    }

    private Map<ItemStack, StorageItem> updateSerializedData(List<StorageItem> items){
        Map<ItemStack, StorageItem> ret = new HashMap<>();
        synchronized (storageData) {
            for (StorageItem si : items) {
                ItemStack deserialized = ItemStack.deserializeBytes(si.getItemData());
                ItemStack updated = deserialized.ensureServerConversions();
                byte[] serialized = updated.serializeAsBytes();
                si.setItemData(serialized);
                ret.put(updated, si);
            }
        }
        return ret;
    }

    public long getUsedCapacity(){
        return itemMappings.values().stream().mapToLong(StorageItem::getAmount).sum();
    }

    public long getTotalCapacity(){
        long baseCapacity = storageData.getCapacity();
        if(storageData.getOwner() == null) return baseCapacity;
        Optional<PlayerStats> optionalPlayerStats = AbstractModule.getInstance(PlayerStats.class);
        if(optionalPlayerStats.isEmpty()) return baseCapacity;
        return baseCapacity + (optionalPlayerStats.get().getOrCreate(storageData.getOwner()).getPoints() / 2L);
    }

    public long getRemainingCapacity(){
        return getTotalCapacity() - getUsedCapacity();
    }

    private ItemStack normalize(ItemStack is){
        return is.getAmount() == 1 ? is : is.asQuantity(1);
    }

    public void addItem(ItemStack item, int amount){
        if(amount <= 0) return;
        item = normalize(item);
        final ItemStack is = item;
        StorageItem si = itemMappings.computeIfAbsent(item, itemStack -> {
            StorageItem newEntry = new StorageItem(storageData, 0, is.serializeAsBytes());
            synchronized (storageData) {
                storageData.getItems().add(newEntry);
            }
            return newEntry;
        });
        si.setAmount(si.getAmount() <= Long.MAX_VALUE - amount ? si.getAmount() + amount : Long.MAX_VALUE);
    }

    public int removeItem(ItemStack item, int maxAmount){
        item = normalize(item);
        StorageItem si = itemMappings.get(item);
        if(si == null) return 0;
        int toRemove = (int) Math.min(maxAmount, si.getAmount());
        si.setAmount(si.getAmount() - toRemove);
        if(si.getAmount() <= 0)
            deleteItem(item);
        return toRemove;
    }

    public long getAmount(ItemStack item){
        item = normalize(item);
        StorageItem si = itemMappings.get(item);
        if(si == null) return 0L;
        return si.getAmount();
    }

    private void deleteItem(ItemStack key){
        StorageItem si = itemMappings.remove(key);
        if(si == null) return;
        synchronized (storageData) {
            storageData.getItems().remove(si);
        }
    }

    public int getDistinctItemCount(){
        return itemMappings.keySet().size();
    }

    public int getPages(){
        return (getDistinctItemCount() / 45) + 1;
    }

    public boolean hasOpenInterfaces(){
        return !openInterfaces.isEmpty();
    }

    public List<StorageInterface> getOpenInterfaces(){
        return openInterfaces;
    }

    public StorageInterface openInterface(Player p){
        StorageInterface si = new StorageInterface(p, this);
        openInterfaces.add(si);
        si.open();
        return si;
    }

    public void closeInterface(StorageInterface storageInterface){
        lastOpened = System.currentTimeMillis();
        openInterfaces.remove(storageInterface);
    }

    public long getLastOpened(){
        return lastOpened;
    }

    public void scheduleSave(){
        Bukkit.getScheduler().runTaskAsynchronously(NeincraftPlugin.getInstance(), this::save);
    }

    public void save(){
        synchronized (storageData) {
            try (StorageRepository repository = StorageRepository.getRepository()) {
                if (repository == null) return;
                repository.save(storageData);
                repository.commit();
            } catch (Exception e) {
                NeincraftUtils.getLogger().log(Level.WARNING, "Could not save storage", e);
            }
        }
    }

    public List<ItemStack> getPage(int page){
        return itemMappings.keySet().stream().sorted(this::compareItems).skip(page * 45L).limit(45).toList();
    }

}
