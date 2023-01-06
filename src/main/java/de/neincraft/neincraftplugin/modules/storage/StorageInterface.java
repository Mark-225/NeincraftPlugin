package de.neincraft.neincraftplugin.modules.storage;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.util.lang.Lang;
import de.themoep.minedown.adventure.MineDown;
import dev.dbassett.skullcreator.SkullCreator;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StorageInterface implements Listener {

    private static final int PREV_BUTTON = 45;
    private static final int NEXT_BUTTON = 53;
    private static final int SORT_BUTTON = 51;
    private static final int INFO_BOOK = 49;
    private static final NamespacedKey PREFERRED_SORT_KEY = new NamespacedKey(NeincraftPlugin.getInstance(), "preferred_storage_sort");

    private ItemStack makeSortButton(){
        ItemStack is = SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2U0MWM2MDU3MmM1MzNlOTNjYTQyMTIyODkyOWU1NGQ2Yzg1NjUyOTQ1OTI0OWMyNWMzMmJhMzNhMWIxNTE3In19fQ==");
        is.editMeta(im -> {
            im.displayName(Lang.SORT_BUTTON.getComponent(viewer));
            im.lore(List.of(sortMode.getName().getComponent(viewer),
                    Lang.SORT_CLICK_TO_CHANGE.getComponent(viewer)));
        });
        return is;
    }

    private static ItemStack makePrevButton(Player player, boolean enabled){
        ItemStack is = SkullCreator.itemFromBase64(enabled ? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODU1MGI3Zjc0ZTllZDc2MzNhYTI3NGVhMzBjYzNkMmU4N2FiYjM2ZDRkMWY0Y2E2MDhjZDQ0NTkwY2NlMGIifX19" :
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY5NzFkZDg4MWRiYWY0ZmQ2YmNhYTkzNjE0NDkzYzYxMmY4Njk2NDFlZDU5ZDFjOTM2M2EzNjY2YTVmYTYifX19");
        is.editMeta(im -> {
            im.displayName(Lang.STORAGE_PREV_PAGE.getComponent(player));
        });
        return is;
    }

    private static ItemStack makeNextButton(Player player, boolean enabled){
        ItemStack is = SkullCreator.itemFromBase64(enabled ? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYzMzlmZjJlNTM0MmJhMThiZGM0OGE5OWNjYTY1ZDEyM2NlNzgxZDg3ODI3MmY5ZDk2NGVhZDNiOGFkMzcwIn19fQ==" :
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMyY2E2NjA1NmI3Mjg2M2U5OGY3ZjMyYmQ3ZDk0YzdhMGQ3OTZhZjY5MWM5YWMzYTkxMzYzMzEzNTIyODhmOSJ9fX0=");
        is.editMeta(im -> {
            im.displayName(Lang.STORAGE_NEXT_PAGE.getComponent(player));
        });
        return is;
    }

    private ItemStack makeInfoBook(){
        ItemStack is = new ItemStack(Material.BOOK);
        is.editMeta(im -> {
           im.displayName(Lang.STORAGE_INFO_ITEM.getComponent(viewer));
           im.lore(List.of(Lang.STORAGE_CAPACITY.getMinedown(viewer).replace(
                   "used", "" + storage.getUsedCapacity(),
                   "total", "" + storage.getTotalCapacity()
                    ).toComponent(),
                   Lang.STORAGE_PAGE.getMinedown(viewer).replace(
                           "current", "" + (currentPage + 1),
                           "total", "" + storage.getPages()
                   ).toComponent()));
           im.addEnchant(Enchantment.DURABILITY, 1, true);
           im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
        return is;
    }

    private Inventory inventory;
    private Storage storage;
    private List<ItemStack> currentItems;
    private int currentPage = 0;
    private Player viewer;
    private SortMode sortMode = SortMode.CREATIVE_TABS;

    public StorageInterface(Player viewer, Storage storage){
        inventory = Bukkit.createInventory(null, 9*6, Lang.STORAGE_CAPTION.getComponent(viewer));
        this.storage = storage;
        this.viewer = viewer;
        PersistentDataContainer pdc = viewer.getPersistentDataContainer();
        String preferredSortMode = pdc.getOrDefault(PREFERRED_SORT_KEY, PersistentDataType.STRING, SortMode.CREATIVE_TABS.name());
        try {
            SortMode pSortMode = SortMode.valueOf(preferredSortMode);
            this.sortMode = pSortMode;
        }catch(IllegalArgumentException e){
            //Enum:valueOf throws an IllegalArgumentException if the given name does not exist... ._.
        }
    }

    public void open(){
        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
        updateInventory();
        viewer.openInventory(inventory);
    }

    private void updateInventory(){
        inventory.clear();
        updateItems();
        generateControls();
    }

    private void updateItems(){
        List<ItemStack> items = storage.getPage(currentPage, sortMode);
        this.currentItems = items;
        Component prefix = Lang.STORAGE_ITEM_AMOUNT_PREFIX.getComponent(viewer);
        for(int i = 0; i < 45 && i < items.size(); i++){
            inventory.setItem(i, makeDisplayItem(items.get(i), prefix));
        }
    }

    private ItemStack makeDisplayItem(ItemStack storageItem, Component prefix){
        ItemStack displayItem = storageItem.clone();
        displayItem.editMeta(im -> {
            List<Component> lore = im.hasLore() ? new ArrayList<>(Objects.requireNonNull(im.lore())) : new ArrayList<>();
            lore.add(prefix);
            lore.add(new MineDown("&gold&" + storage.getAmount(storageItem)).toComponent());
            im.lore(lore);
        });
        return displayItem;
    }

    private void generateControls(){
        inventory.setItem(PREV_BUTTON, makePrevButton(viewer, currentPage > 0));
        inventory.setItem(NEXT_BUTTON, makeNextButton(viewer, currentPage < storage.getPages() - 1));
        inventory.setItem(INFO_BOOK, makeInfoBook());
        inventory.setItem(SORT_BUTTON, makeSortButton());
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event){
        if(event.getView().getTopInventory() != inventory) return;
        event.setCancelled(true);
        if(event.getWhoClicked() != viewer) return;
        if(event.getClick() != ClickType.LEFT && event.getClick() != ClickType.SHIFT_LEFT) return;
        if(event.getClickedInventory() != inventory && event.getClickedInventory() != viewer.getInventory()) return;
        ClickType click = event.getClick();

        if(event.getClickedInventory() == viewer.getInventory()){
            handlePlayerInventoryClick(event.getSlot(), click);
            return;
        }

        if(event.getSlot() < 45){
            handleStorageClick(event.getSlot(), click);
            return;
        }

        switch(event.getSlot()){
            case PREV_BUTTON -> {
                switchPage(-1);
            }
            case NEXT_BUTTON -> {
                switchPage(1);
            }
            case SORT_BUTTON -> {
                cycleSortMode();
            }
        }
    }

    private void cycleSortMode(){
        currentPage = 0;
        sortMode = SortMode.values()[(sortMode.ordinal() + 1) % SortMode.values().length];
        viewer.getPersistentDataContainer().set(PREFERRED_SORT_KEY, PersistentDataType.STRING, sortMode.name());
        updateInventory();
    }

    private void handlePlayerInventoryClick(int slot, ClickType click){
        ItemStack clickedItem = viewer.getInventory().getItem(slot);
        if(clickedItem == null || clickedItem.getType() == Material.AIR) return;

        int amount = 1;
        if(click == ClickType.SHIFT_LEFT)
            amount = clickedItem.getAmount();

        long capacity = storage.getRemainingCapacity();
        if(capacity < amount)
            amount = (int) capacity;
        if(amount < 0) amount = 0;

        storage.addItem(clickedItem.asOne(), amount);
        clickedItem.setAmount(clickedItem.getAmount()-amount);
        updateInventory();
    }

    private void handleStorageClick(int slot, ClickType click){
        if(slot >= currentItems.size()) return;
        ItemStack clickedItem = currentItems.get(slot);
        int amount = 1;
        if(click == ClickType.SHIFT_LEFT)
            amount = clickedItem.getMaxStackSize();
        int removedAmount = storage.removeItem(clickedItem, amount);
        if(removedAmount <= 0) return;
        ItemStack toAdd = clickedItem.asQuantity(removedAmount);
        viewer.getInventory().addItem(toAdd).values().forEach(overflow -> viewer.getWorld().dropItem(viewer.getLocation(), overflow));
        updateInventory();
    }

    private void switchPage(int delta){
        currentPage += delta;
        if(currentPage < 0) currentPage = 0;
        if(currentPage >= storage.getPages()) currentPage = storage.getPages()-1;
        updateInventory();
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event){
        if(event.getInventory() != inventory) return;
        storage.scheduleSave();
        close();
    }

    public void close(){
        HandlerList.unregisterAll(this);
        inventory.close();
        storage.closeInterface(this);
    }

    public static enum SortMode{

        CREATIVE_TABS(Lang.SORT_CREATIVE),
        AMOUNT(Lang.SORT_AMOUNT),
        ALPHABETICAL(Lang.SORT_ALPHABET);

        private Lang name;

        SortMode(Lang name){
            this.name = name;
        }

        public Lang getName(){
            return  name;
        }
    }


}
