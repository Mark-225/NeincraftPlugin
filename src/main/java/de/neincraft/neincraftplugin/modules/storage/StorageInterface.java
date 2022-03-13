package de.neincraft.neincraftplugin.modules.storage;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.util.Lang;
import de.themoep.minedown.adventure.MineDown;
import dev.dbassett.skullcreator.SkullCreator;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StorageInterface implements Listener {

    private static final int PREV_BUTTON = 45;
    private static final int NEXT_BUTTON = 53;
    private static final int INFO_BOOK = 49;

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

    public StorageInterface(Player viewer, Storage storage){
        inventory = Bukkit.createInventory(null, 9*6, Lang.STORAGE_CAPTION.getComponent(viewer));
        this.storage = storage;
        this.viewer = viewer;
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
        List<ItemStack> items = storage.getPage(currentPage);
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
        }
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


}
