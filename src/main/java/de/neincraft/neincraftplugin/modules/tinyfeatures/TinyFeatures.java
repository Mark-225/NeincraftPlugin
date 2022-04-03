package de.neincraft.neincraftplugin.modules.tinyfeatures;

import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.NeincraftModule;
import de.neincraft.neincraftplugin.modules.blockentityproperties.BEProperty;
import de.neincraft.neincraftplugin.modules.blockentityproperties.BlockEntityProperties;
import de.neincraft.neincraftplugin.modules.commands.InjectCommand;
import de.neincraft.neincraftplugin.util.Lang;
import de.themoep.minedown.adventure.MineDown;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.units.qual.N;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@NeincraftModule(id = "TinyFeatures")
public class TinyFeatures extends AbstractModule implements Listener {

    private static final NamespacedKey SOULBOUND_KEY = new NamespacedKey(NeincraftPlugin.getInstance(), "enchantment-soulbound");

    @InjectCommand("echo")
    private EchoCommand echoCommand;

    @Override
    protected boolean initModule() {
        Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());
        startPlayerListTask();
        return true;
    }

    private void startPlayerListTask(){
        Bukkit.getScheduler().runTaskTimer(NeincraftPlugin.getInstance(), () -> {
            String tps = new DecimalFormat("#0.##").format(Bukkit.getTPS()[0]);
            int players = Bukkit.getOnlinePlayers().size();
            int slots = Bukkit.getMaxPlayers();
            String usedMem = new DecimalFormat("#0.00").format((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1_073_741_824d);
            String maxMem = new DecimalFormat("#0.00").format(Runtime.getRuntime().maxMemory() / 1_073_741_824d);
            for(Player p : Bukkit.getOnlinePlayers()){
                p.sendPlayerListHeaderAndFooter(Lang.PLAYER_LIST_HEADER.getComponent(p),
                        Lang.PLAYER_LIST_FOOTER.getMinedown(p).replace(
                                "tps", tps,
                                "players", ""+players,
                                "slots", ""+slots,
                                "used", usedMem,
                                "allocated", maxMem
                        ).toComponent());
            }
        }, 0, 200);
    }

    //Anvil Dispensers

    @EventHandler(priority = EventPriority.HIGH)
    public void onDispenserDispenseAnvil(BlockPreDispenseEvent event){
        if(event.isCancelled()) return;
        if(event.getBlock().getType() != Material.DISPENSER) return;
        if(event.getBlock().getBlockData() instanceof Directional directional && event.getBlock().getState(false) instanceof Dispenser dispenser) {
            ItemStack item = dispenser.getInventory().getItem(event.getSlot());
            if(item == null || item.getType() != Material.ANVIL) return;
            event.setCancelled(true);
            Block target = event.getBlock().getRelative(directional.getFacing());
            if(target.getType() != Material.AIR && target.getType() != Material.CAVE_AIR) return;
            target.setType(Material.ANVIL);
            event.getBlock().getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
            Optional<BlockEntityProperties> beProperties = AbstractModule.getInstance(BlockEntityProperties.class);
            if(beProperties.isEmpty() || !beProperties.get().getProperty(dispenser, BEProperty.INFDISP)) item.setAmount(item.getAmount()-1);
        }
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
        if(event.getHand() != EquipmentSlot.HAND || !event.getPlayer().isSneaking()) return;
        if(event.getItem() != null) return;
        if(!Tag.SIGNS.isTagged(event.getClickedBlock().getType())) return;
        if(!event.getPlayer().hasPermission("neincraft.editsigns")) return;
        if(event.getClickedBlock().getState(false) instanceof Sign sign){
            event.getPlayer().openSign(sign);
            event.setCancelled(true);
        }
    }

    //Leashable Villagers
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event){
        if(!(event.getRightClicked() instanceof Villager)) return;
        ItemStack item = event.getPlayer().getEquipment().getItem(event.getHand());
        Villager villager = (Villager) event.getRightClicked();
        if(!villager.isLeashed() && item != null && item.getType() == Material.LEAD){
            event.setCancelled(true);
            PlayerLeashEntityEvent leashEvent = new PlayerLeashEntityEvent(villager, event.getPlayer(), event.getPlayer());
            Bukkit.getPluginManager().callEvent(leashEvent);
            if(leashEvent.isCancelled()) return;
            villager.setLeashHolder(event.getPlayer());
            if(event.getPlayer().getGameMode() != GameMode.CREATIVE) item.setAmount(item.getAmount()-1);
            return;
        }
        if(villager.isLeashed() && event.getHand() == EquipmentSlot.OFF_HAND) event.setCancelled(true);
    }

    //Respawn fix
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        if(!event.isAnchorSpawn() && !event.isBedSpawn()){
            event.setRespawnLocation(event.getPlayer().getWorld().getSpawnLocation());
        }
    }

    //Infinite Villagers
    @EventHandler
    public void onPlayerTrade(PlayerTradeEvent event){
        if(event.getVillager().hasAI()) return;
        event.setIncreaseTradeUses(false);
        event.setRewardExp(false);
    }

    //soulbound
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        List<ItemStack> soulboundDrops = event.getDrops().stream().filter(Objects::nonNull).filter(ItemStack::hasItemMeta).filter(is -> is.getItemMeta().getPersistentDataContainer().has(SOULBOUND_KEY)).toList();
        event.getDrops().removeAll(soulboundDrops);
        event.getItemsToKeep().addAll(soulboundDrops);
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event){
        AnvilInventory inv = event.getInventory();
        if(inv.getFirstItem() == null || inv.getFirstItem().getItemMeta().getPersistentDataContainer().has(SOULBOUND_KEY)) return;
        if(inv.getSecondItem() == null || inv.getSecondItem().getType() != Material.DRAGON_EGG || inv.getSecondItem().getAmount() != 1) return;
        ItemStack target = inv.getFirstItem().clone();
        target.editMeta(im -> {
            im.getPersistentDataContainer().set(SOULBOUND_KEY, PersistentDataType.BYTE, (byte) 1);
            List<Component> lore = new ArrayList<>();
            if(im.hasLore())
                lore.addAll(im.lore());
            lore.add(new MineDown("&gray&Soulbound").toComponent());
            im.lore(lore);
        });
        inv.setRepairCost(1);
        event.setResult(target);
    }

    @Override
    public void unload() {

    }
}
