package de.neincraft.neincraftplugin.modules.unbreakable;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

@NeincraftModule(id = "UnbreakableItems")
public class UnbreakableItems extends AbstractModule implements Listener {
    @Override
    protected boolean initModule() {
        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
        return true;
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event){
        if(event.getItem().getType() == Material.ELYTRA) return;
        if(event.getItem().getEnchantmentLevel(Enchantment.DURABILITY) < 3) return;

        if(event.getItem().hasItemMeta() && event.getItem().getItemMeta() instanceof Damageable damageable){
            if(damageable.getDamage() + event.getDamage() < event.getItem().getType().getMaxDurability()) return;
            event.setDamage(event.getItem().getType().getMaxDurability() - damageable.getDamage() - 1);
            if(event.getDamage() == 0) event.setCancelled(true);
            event.getPlayer().playSound(event.getPlayer(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            Bukkit.getScheduler().runTask(NeincraftPlugin.getInstance(), () -> unequipBrokenArmor(event.getPlayer()));
        }
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event){
        if(event.getItem() == null) return;
        if(event.getItem().getEnchantmentLevel(Enchantment.DURABILITY) < 3) return;
        if(event.getItem().hasItemMeta() && event.getItem().getItemMeta() instanceof Damageable damageable){
            if(damageable.getDamage() >= event.getItem().getType().getMaxDurability() - 1) {
                event.setCancelled(true);
                event.getPlayer().sendActionBar(Lang.ITEM_BROKE.getComponent(event.getPlayer()));
            }
        }
    }

    @EventHandler
    public void onArmorEquip(PlayerArmorChangeEvent event){
        Bukkit.getScheduler().runTask(NeincraftPlugin.getInstance(), () -> unequipBrokenArmor(event.getPlayer()));
    }

    @EventHandler
    public void onDispenserArmor(BlockDispenseArmorEvent event){
        if(event.getTargetEntity() instanceof Player player){
            Bukkit.getScheduler().runTask(NeincraftPlugin.getInstance(), () -> unequipBrokenArmor(player));
        }
    }

    private void unequipBrokenArmor(Player player){
        ItemStack[] newArmorContents = player.getInventory().getArmorContents().clone();
        boolean update = false;
        for(int i = 0; i < player.getInventory().getArmorContents().length; i++){
            ItemStack is = player.getInventory().getArmorContents()[i];
            if(is == null || is.getType() == Material.AIR || is.getEnchantmentLevel(Enchantment.DURABILITY) < 3) continue;
            if(is.hasItemMeta() && is.getItemMeta() instanceof Damageable damageable){
                if(damageable.getDamage() >= is.getType().getMaxDurability() - 1){
                        ItemStack item = is.clone();
                        newArmorContents[i] = null;
                        update = true;
                        player.getInventory().addItem(item).values().forEach(overflow -> player.getWorld().dropItem(player.getLocation(), overflow));
                }
            }
        }
        if(update) {
            player.getInventory().setArmorContents(newArmorContents);
            player.sendActionBar(Lang.ARMOR_BROKE.getComponent(player));
        }
    }

    @Override
    public void unload() {

    }
}
