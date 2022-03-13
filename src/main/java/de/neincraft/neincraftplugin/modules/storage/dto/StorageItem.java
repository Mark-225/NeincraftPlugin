package de.neincraft.neincraftplugin.modules.storage.dto;

import de.neincraft.neincraftplugin.util.NeincraftUtils;
import org.bukkit.inventory.ItemStack;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "storage_item")
public class StorageItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private StorageSystem storageSystem;

    @Column
    private long amount;

    @Column
    @Lob
    private byte[] itemData;

    public StorageItem() {
    }

    public StorageItem(StorageSystem storageSystem, long amount, byte[] itemData) {
        this.storageSystem = storageSystem;
        this.amount = amount;
        this.itemData = itemData;
    }

    public long getId() {
        return id;
    }

    public StorageSystem getStorageSystem() {
        return storageSystem;
    }

    public void setStorageSystem(StorageSystem storageSystem) {
        this.storageSystem = storageSystem;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public byte[] getItemData() {
        return itemData;
    }

    public void setItemData(byte[] itemData) {
        this.itemData = itemData;
    }

    public ItemStack getNormalizedBukkitItem() {
        return ItemStack.deserializeBytes(getItemData());
    }
}