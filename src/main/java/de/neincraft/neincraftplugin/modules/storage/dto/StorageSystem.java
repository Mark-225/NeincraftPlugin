package de.neincraft.neincraftplugin.modules.storage.dto;

import de.neincraft.neincraftplugin.modules.database.util.UUIDDataType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "storage_system")
public class StorageSystem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long storageId;

    @Column
    @Type(UUIDDataType.class)
    UUID owner;

    @Column
    long capacity;

    @OneToMany(mappedBy = "storageSystem", orphanRemoval = true, cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    List<StorageItem> items;

    public StorageSystem(UUID owner, long capacity, List<StorageItem> items) {
        this.owner = owner;
        this.capacity = capacity;
        this.items = items;
    }

    public StorageSystem() {
    }

    public long getStorageId() {
        return storageId;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public List<StorageItem> getItems() {
        return items;
    }

    public void setItems(List<StorageItem> items) {
        this.items = items;
    }
}