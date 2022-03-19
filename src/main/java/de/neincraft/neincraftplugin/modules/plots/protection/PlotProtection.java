package de.neincraft.neincraftplugin.modules.plots.protection;

import com.destroystokyo.paper.MaterialTags;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import de.neincraft.neincraftplugin.NeincraftPlugin;
import de.neincraft.neincraftplugin.modules.AbstractModule;
import de.neincraft.neincraftplugin.modules.blockentityproperties.BEProperty;
import de.neincraft.neincraftplugin.modules.playerstats.PlayerLanguage;
import de.neincraft.neincraftplugin.modules.plots.Plot;
import de.neincraft.neincraftplugin.modules.plots.PlotModule;
import de.neincraft.neincraftplugin.modules.plots.dto.SubdivisionData;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.ChunkKey;
import de.neincraft.neincraftplugin.modules.plots.util.PlotPermission;
import de.neincraft.neincraftplugin.modules.plots.util.PlotSetting;
import de.neincraft.neincraftplugin.util.BlockBatchChangedEvent;
import de.neincraft.neincraftplugin.util.Lang;
import de.neincraft.neincraftplugin.util.NeincraftUtils;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import io.papermc.paper.event.block.PlayerShearBlockEvent;
import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Lectern;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PlotProtection implements Listener {

    public final NamespacedKey public_container = new NamespacedKey(NeincraftPlugin.getInstance(), "public_access");

    private Map<ChunkKey, Optional<Plot>> plotCache = new HashMap<>();
    private PlotModule plotModule;
    private List<UUID> playersAtIllegalLocation = new ArrayList<>();
    private BossBar pvpBossbarEN = null;
    private BossBar pvpBossbarDE = null;

    public PlotProtection(){
        AbstractModule.getInstance(PlotModule.class).ifPresent(pm ->{
            plotModule = pm;
            Bukkit.getPluginManager().registerEvents(this, NeincraftPlugin.getInstance());

            Bukkit.getScheduler().runTaskTimer(NeincraftPlugin.getInstance(), () ->{
                playersAtIllegalLocation.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
                for(Player p : Bukkit.getOnlinePlayers()){
                    Optional<Plot> plot = getFromCache(p.getChunk());
                    ChunkKey playerChunk = ChunkKey.fromChunk(p.getChunk());
                    if(plot.isPresent() && !p.hasPermission("neincraft.plots.bypass.enter") && !plot.get().resolveSettingsValue(plot.get().getChunkData(playerChunk).getSubdivision(), PlotSetting.ALLOW_ENTER) && !p.getUniqueId().equals(plot.get().getPlotData().getOwner())){
                        if(playersAtIllegalLocation.contains(p.getUniqueId())) {
                            p.teleport(p.getWorld().getSpawnLocation().add(0.5, 0, 0.5));
                        }
                        else {
                            playersAtIllegalLocation.add(p.getUniqueId());
                            continue;
                        }
                    }
                    if(plot.isPresent() && plot.get().resolveSettingsValue(plot.get().getChunkData(playerChunk).getSubdivision(), PlotSetting.ALLOW_PVP)){
                        showPvPWarning(p);
                    }else{
                        hidePvPWarning(p);
                    }
                    playersAtIllegalLocation.remove(p.getUniqueId());
                }
            }, 100, 100);
            pvpBossbarEN = BossBar.bossBar(Lang.PLOT_PVP_ACTIVE.getComponent(PlayerLanguage.ENGLISH, null), BossBar.MAX_PROGRESS, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
            pvpBossbarDE = BossBar.bossBar(Lang.PLOT_PVP_ACTIVE.getComponent(PlayerLanguage.GERMAN, null), BossBar.MAX_PROGRESS, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        });
    }

    private void showPvPWarning(Player p){
        PlayerLanguage language = NeincraftUtils.resolvePlayerLanguage(p);
        if(language == PlayerLanguage.GERMAN){
            p.hideBossBar(pvpBossbarEN);
            p.showBossBar(pvpBossbarDE);
        }else{
            p.hideBossBar(pvpBossbarDE);
            p.showBossBar(pvpBossbarEN);
        }
    }

    private void hidePvPWarning(Player p){
        p.hideBossBar(pvpBossbarDE);
        p.hideBossBar(pvpBossbarEN);
    }

    private Optional<Plot> getFromCache(ChunkKey chunk){
        return plotCache.computeIfAbsent(chunk, key -> plotModule.getPlotAtChunk(key));
    }

    private Optional<Plot> getFromCache(Chunk chunk){
        return getFromCache(ChunkKey.fromChunk(chunk));
    }

    private boolean canPropagate(Plot from, Plot to){
        if(from == to || to == null) return true;
        if(from == null) return false;
        return Objects.equals(from.getPlotData().getOwner(), to.getPlotData().getOwner());
    }

    @EventHandler
    public void onTickStart(ServerTickStartEvent event){
        plotCache.clear();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        if(event.getFrom().getChunk() == event.getTo().getChunk()) return;
        Optional<Plot> newPlot = getFromCache(event.getTo().getChunk());
        Optional<Plot> oldPlot = getFromCache(event.getFrom().getChunk());
        if(newPlot.isEmpty() && oldPlot.isEmpty()) return;
        if(newPlot.isPresent() && newPlot.equals(oldPlot)){
            SubdivisionData oldSubdivision = newPlot.get().getChunkData(ChunkKey.fromChunk(event.getFrom().getChunk())).getSubdivision();
            SubdivisionData newSubdivision = newPlot.get().getChunkData(ChunkKey.fromChunk(event.getTo().getChunk())).getSubdivision();
            if(oldSubdivision == newSubdivision) return;
        }
        newPlot.ifPresentOrElse(
                plot -> {
                    ChunkKey targetChunk = ChunkKey.fromChunk(event.getTo().getChunk());
                    SubdivisionData targetSubdivision = plot.getChunkData(targetChunk).getSubdivision();
                    if(!event.getPlayer().hasPermission("neincraft.plots.bypass.enter") && !plot.resolveSettingsValue(targetSubdivision, PlotSetting.ALLOW_ENTER) && !event.getPlayer().getUniqueId().equals(plot.getPlotData().getOwner())){
                        event.setCancelled(true);
                        event.getPlayer().sendActionBar(Lang.PLOT_CANT_ENTER.getComponent(event.getPlayer()));
                        return;
                    }
                    if(plot.resolveSettingsValue(targetSubdivision, PlotSetting.ALLOW_PVP)){
                        showPvPWarning(event.getPlayer());
                    }else{
                        hidePvPWarning(event.getPlayer());
                    }
                    event.getPlayer().sendActionBar(Lang.PLOT_ENTER.getMinedown(event.getPlayer()).replace(
                            "plot", plot.getPlotData().getName(),
                            "owner", plot.isServerPlot() ? "Server" : NeincraftUtils.uuidToName(plot.getPlotData().getOwner()),
                            "subdivision", plot.getChunkData(ChunkKey.fromChunk(event.getTo().getChunk())).getSubdivision().getSubdivisionId().getName()
                    ).toComponent());
                },
                () ->{
                    hidePvPWarning(event.getPlayer());
                    event.getPlayer().sendActionBar(Lang.PLOT_LEAVE.getComponent(event.getPlayer()));
                }
        );
    }

    private void handleBasicPlayerEvent(Player player, PlotPermission requiredPermission, Chunk changedChunk, Consumer<Boolean> cancel){
        if(player.hasPermission("neincraft.plots.bypass." + requiredPermission.getBypassPermission())) return;
        Optional<Plot> oPlot = getFromCache(changedChunk);
        if(oPlot.isEmpty()) return;
        Plot plot = oPlot.get();
        if(!plot.resolvePermission(plot.getChunkData(ChunkKey.fromChunk(changedChunk)).getSubdivision(), plot.getPlayerGroup(player.getUniqueId()), requiredPermission)){
            cancel.accept(true);
            player.sendActionBar(Lang.PLOT_CANT_MODIFY.getComponent(player));
        }
    }

    private void handleBatchChangeEvent(Chunk sourceChunk, List<Block> blocks, boolean removeFromList, Consumer<Boolean> cancel){
        Optional<Plot> sourcePlot = getFromCache(sourceChunk);
        if(removeFromList){
            blocks.removeIf(block -> !canPropagate(sourcePlot.orElse(null), getFromCache(block.getChunk()).orElse(null)));
        }else{
            if(blocks.stream().anyMatch(block -> !canPropagate(sourcePlot.orElse(null), getFromCache(block.getChunk()).orElse(null))))
                cancel.accept(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getBlock().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getBlock().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event){
        if(event.getRemover() instanceof Player player){
            handleBasicPlayerEvent(player, PlotPermission.BUILD, event.getEntity().getChunk(), event::setCancelled);
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event){
        if(event.getPlayer() == null) return;
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getEntity().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event){
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getBlock().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event){
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getBlock().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onItemFrameChange(PlayerItemFrameChangeEvent event){
        if(event.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.ROTATE){
            handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getItemFrame().getChunk(), event::setCancelled);
        }else{
            handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getItemFrame().getChunk(), event::setCancelled);
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event){
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getRightClicked().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onHarvestBlock(PlayerHarvestBlockEvent event){
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getHarvestedBlock().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onTrampleSoil(PlayerInteractEvent event){
        if(event.getClickedBlock() == null) return;
        if(event.getAction() != Action.PHYSICAL || event.getClickedBlock().getType() != Material.FARMLAND) return;
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getClickedBlock().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onPlayerTrade(PlayerInteractEntityEvent event){
        if(!(event.getRightClicked() instanceof AbstractVillager)) return;
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.TRADE_VILLAGERS, event.getRightClicked().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onShearEntity(PlayerShearEntityEvent event){
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.SHEAR_ENTITIES, event.getEntity().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onShearBlock(PlayerShearBlockEvent event){
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getBlock().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onInteractBlocks(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
        Material material = event.getClickedBlock().getType();
        PlotPermission permission = null;
        if(Tag.WOODEN_BUTTONS.isTagged(material)){
            permission = PlotPermission.USE_WOOD_BUTTONS;
        }else if(material == Material.STONE_BUTTON){
            permission = PlotPermission.USE_STONE_BUTTONS;
        }else if(material == Material.LEVER){
            permission = PlotPermission.USE_LEVERS;
        }else if(material == Material.REDSTONE_WIRE || material == Material.COMPARATOR || material == Material.REPEATER){
            permission = PlotPermission.USE_REDSTONE_COMPONENTS;
        }else if(material == Material.JUKEBOX || material == Material.NOTE_BLOCK){
            permission = PlotPermission.USE_JUKEBOX;
        }else if(Tag.ANVIL.isTagged(material)){
            permission = PlotPermission.USE_ANVILS;
        }else if(material == Material.CRAFTING_TABLE){
            permission = PlotPermission.USE_CRAFTING_TABLE;
        }else if(material == Material.ENDER_CHEST){
            permission = PlotPermission.USE_ENDERCHESTS;
        }else if(Tag.WOODEN_DOORS.isTagged(material)){
            permission = PlotPermission.USE_WOODEN_DOORS;
        }else if(Tag.FENCE_GATES.isTagged(material) || Tag.WOODEN_TRAPDOORS.isTagged(material)){
            permission = PlotPermission.USE_OTHER_OPENABLES;
        }else if(material == Material.LECTERN){
            permission = ((Lectern) event.getClickedBlock().getBlockData()).hasBook() ? PlotPermission.READ_LECTERN :PlotPermission.EDIT_LECTERN;
        }else if(material == Material.BEEHIVE){
            permission = PlotPermission.HARVEST_HIVES;
        }else if(Tag.SIGNS.isTagged(material) && event.getItem() != null && MaterialTags.DYES.isTagged(event.getItem().getType())){
            permission = PlotPermission.BUILD;
        }else if(material == Material.BEACON){
            permission = PlotPermission.BUILD;
        }

        if(permission != null)
            handleBasicPlayerEvent(event.getPlayer(), permission, event.getClickedBlock().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onPlayerTakeBook(PlayerTakeLecternBookEvent event){
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.EDIT_LECTERN, event.getLectern().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onProjectileThrow(PlayerLaunchProjectileEvent event){
        PlotPermission permission = null;
        if(Tag.ENTITY_TYPES_ARROWS.isTagged(event.getProjectile().getType())){
            permission = PlotPermission.FIRE_ARROWS;
        }else{
            permission = PlotPermission.THROW_PROJECTILES;
        }
        handleBasicPlayerEvent(event.getPlayer(), permission, event.getPlayer().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event){
        if(event.getEntity().getShooter() == null || !(event.getEntity().getShooter() instanceof Player)) return;
        Chunk chunk = event.getHitBlock() != null ? event.getHitBlock().getChunk() : event.getHitEntity() != null ? event.getHitEntity().getChunk() : event.getEntity().getChunk();
        handleBasicPlayerEvent((Player) event.getEntity().getShooter(), Tag.ENTITY_TYPES_ARROWS.isTagged(event.getEntity().getType()) ? PlotPermission.FIRE_ARROWS : PlotPermission.THROW_PROJECTILES, chunk, event::setCancelled);
    }

    @EventHandler
    public void onEntityDamaged(EntityDamageByEntityEvent event){
        Player player = null;
        if(event.getDamager() instanceof Player p)
            player = p;
        else if(event.getDamager() instanceof Projectile pr && pr.getShooter() instanceof Player p)
            player = p;
        if(player != null)
            handleBasicPlayerEvent(player, PlotPermission.HURT_NON_MONSTERS, event.getEntity().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onOpenContainer(InventoryOpenEvent event){
        if(!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if(event.getInventory().getHolder() instanceof Container container){
            NamespacedKey key = new NamespacedKey(NeincraftPlugin.getInstance(), BEProperty.PUBLIC.getKeyIdentifier());
            if(container.getPersistentDataContainer().has(key, PersistentDataType.LONG)){
                Optional<Plot> oPlot = plotModule.getPlotAtChunk(ChunkKey.fromChunk(container.getChunk()));
                Long id = container.getPersistentDataContainer().get(key, PersistentDataType.LONG);
                if(oPlot.isPresent() && id != null && id == oPlot.get().getPlotData().getId()) return;
            }
            handleBasicPlayerEvent(player, PlotPermission.OPEN_CONTAINERS, container.getChunk(), event::setCancelled);
        }else if(event.getInventory().getHolder() instanceof DoubleChest doubleChest && doubleChest.getLeftSide(false) instanceof Container leftContainer && doubleChest.getRightSide(false) instanceof Container rightContainer){
            NamespacedKey key = new NamespacedKey(NeincraftPlugin.getInstance(), BEProperty.PUBLIC.getKeyIdentifier());
            if(leftContainer.getPersistentDataContainer().has(key, PersistentDataType.LONG) && rightContainer.getPersistentDataContainer().has(key, PersistentDataType.LONG)){
                Optional<Plot> oPlot = plotModule.getPlotAtChunk(ChunkKey.fromChunk(leftContainer.getChunk()));
                Long id = leftContainer.getPersistentDataContainer().get(key, PersistentDataType.LONG);
                if(oPlot.isPresent() && id != null && id.equals(rightContainer.getPersistentDataContainer().get(key, PersistentDataType.LONG)) && id == oPlot.get().getPlotData().getId()) return;
            }
            handleBasicPlayerEvent(player, PlotPermission.OPEN_CONTAINERS, leftContainer.getChunk(), event::setCancelled);
            handleBasicPlayerEvent(player, PlotPermission.OPEN_CONTAINERS, rightContainer.getChunk(), event::setCancelled);
        }else if(event.getInventory().getHolder() instanceof Entity entity && (event.getInventory().getHolder() instanceof StorageMinecart || event.getInventory().getHolder() instanceof HopperMinecart || event.getInventory().getHolder() instanceof ChestedHorse)){
            handleBasicPlayerEvent(player, PlotPermission.OPEN_CONTAINERS, entity.getChunk(), event::setCancelled);
        }
    }

    @EventHandler
    public void onMountEntity(EntityMountEvent event){
        if(event.getEntity() instanceof Player player && event.getMount() instanceof Vehicle vehicle){
            handleBasicPlayerEvent(player, PlotPermission.USE_RIDEABLES, vehicle.getChunk(), event::setCancelled);
        }
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent event){
        if(event.getPlayer() == null) return;
        PlotPermission permission;
        if(event.getEntity() instanceof Vehicle){
            permission = PlotPermission.PLACE_RIDEABLES;
        }else{
            permission = PlotPermission.BUILD;
        }
        handleBasicPlayerEvent(event.getPlayer(), permission, event.getEntity().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event){
        if(event.getAttacker() instanceof Player player){
            handleBasicPlayerEvent(player, PlotPermission.PLACE_RIDEABLES, event.getVehicle().getChunk(), event::setCancelled);
        }
    }

    @EventHandler
    public void onSaddleEntity(PlayerInteractEntityEvent event){
        if(!(event.getRightClicked() instanceof AbstractHorse) && !(event.getRightClicked() instanceof Pig)) return;
        if(event.getPlayer().getEquipment().getItem(event.getHand()).getType() != Material.SADDLE) return;
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.SADDLE_ENTITIES, event.getRightClicked().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onLead(PlayerLeashEntityEvent event){
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.USE_LEADS, event.getEntity().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onFertilize(BlockFertilizeEvent event){
        if(event.getPlayer() == null) return;
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getBlock().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onFlowerPotEdit(PlayerFlowerPotManipulateEvent event){
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getFlowerpot().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onHitDragonEgg(PlayerInteractEvent event){
        if((event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.DRAGON_EGG)
            handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getClickedBlock().getChunk(), event::setCancelled);
    }

    @EventHandler
    public void onSignDye(SignChangeEvent event){
        handleBasicPlayerEvent(event.getPlayer(), PlotPermission.BUILD, event.getBlock().getChunk(), event::setCancelled);
    }


    //global

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event){
        Optional<Plot> sourcePlot = getFromCache(event.getLocation().getChunk());
        event.getBlocks().removeIf(bs -> !canPropagate(sourcePlot.orElse(null), getFromCache(bs.getChunk()).orElse(null)));
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event){
        if(!canPropagate(getFromCache(event.getSource().getChunk()).orElse(null), getFromCache(event.getBlock().getChunk()).orElse(null)))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSpongeAbsorb(SpongeAbsorbEvent event){
        Optional<Plot> sourcePlot = getFromCache(event.getBlock().getChunk());
        event.getBlocks().removeIf(bs -> !canPropagate(sourcePlot.orElse(null), getFromCache(bs.getChunk()).orElse(null)));
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event){
        if(!canPropagate(getFromCache(event.getBlock().getChunk()).orElse(null), getFromCache(event.getToBlock().getChunk()).orElse(null)))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockLand(EntityChangeBlockEvent event){
        if(event.getTo() == Material.AIR) return;
        if(event.getEntity() instanceof FallingBlock fallingBlock){
            Location origin = fallingBlock.getOrigin();
            if(origin == null) return;
            if(!canPropagate(getFromCache(origin.getChunk()).orElse(null), getFromCache(fallingBlock.getChunk()).orElse(null)))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){
        final boolean isPublicWorld = plotModule.isPublicWorld(event.getEntity().getWorld());
        Optional<Plot> sourcePlot = getFromCache(event.getEntity().getOrigin().getChunk());
        Optional<Plot> targetPlot = getFromCache(event.getEntity().getChunk());
        if(!sourcePlot.equals(targetPlot) || (targetPlot.isPresent() && !targetPlot.get().resolveSettingsValue(targetPlot.get().getChunkData(ChunkKey.fromChunk(event.getEntity().getChunk())).getSubdivision(), PlotSetting.ALLOW_EXPLOSIONS))){
            event.setCancelled(true);
            return;
        }
        event.blockList().removeIf(block -> {
            Optional<Plot> plot = getFromCache(block.getChunk());
            if(plot.isEmpty()) return isPublicWorld;
            boolean canPropagate = canPropagate(sourcePlot.orElse(null), plot.orElse(null));
            return !canPropagate || !plot.get().resolveSettingsValue(plot.get().getChunkData(ChunkKey.fromChunk(block.getChunk())).getSubdivision(), PlotSetting.ALLOW_EXPLOSIONS);
        });
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event){
        if(plotModule.isPublicWorld(event.getBlock().getWorld())){
            event.setCancelled(true);
            return;
        }
        Optional<Plot> sourcePlot = getFromCache(event.getBlock().getChunk());
        event.blockList().removeIf(block -> {
            Optional<Plot> plot = getFromCache(block.getChunk());
            boolean canPropagate = canPropagate(sourcePlot.orElse(null), plot.orElse(null));
            return !canPropagate || (plot.isPresent() && !plot.get().resolveSettingsValue(plot.get().getChunkData(ChunkKey.fromChunk(block.getChunk())).getSubdivision(), PlotSetting.ALLOW_EXPLOSIONS));
        });
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION && event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) return;
        Optional<Plot> plot = getFromCache(event.getEntity().getChunk());
        if((plot.isEmpty() && plotModule.isPublicWorld(event.getEntity().getWorld())) ||
                (plot.isPresent() && !plot.get().resolveSettingsValue(plot.get().getChunkData(ChunkKey.fromChunk(event.getEntity().getChunk())).getSubdivision(), PlotSetting.ALLOW_EXPLOSIONS))){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event){
        List<Block> previousBlocks = event.getBlocks();
        List<Block> extendedBlocks = previousBlocks.stream().map(block -> block.getLocation().add(event.getDirection().getDirection()).getBlock()).toList();
        handleBatchChangeEvent(event.getBlock().getChunk(), Stream.concat(previousBlocks.stream(), extendedBlocks.stream()).toList(), false, event::setCancelled);
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event){
        List<Block> previousBlocks = event.getBlocks();
        List<Block> retractedBlocks = previousBlocks.stream().map(block -> block.getLocation().add(event.getDirection().getDirection()).getBlock()).toList();
        handleBatchChangeEvent(event.getBlock().getChunk(), Stream.concat(previousBlocks.stream(), retractedBlocks.stream()).toList(), false, event::setCancelled);
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event){
        if(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;
        PlotSetting setting = null;
        if(event.getEntity() instanceof Monster){
            setting = PlotSetting.SPAWN_MONSTERS;
        }else if(event.getEntity() instanceof Animals){
            setting = PlotSetting.SPAWN_ANIMALS;
        }
        if(setting == null) return;
        Optional<Plot> plot = getFromCache(event.getLocation().getChunk());
        if(plot.isEmpty()) return;
        if(!plot.get().resolveSettingsValue(plot.get().getChunkData(ChunkKey.fromChunk(event.getLocation().getChunk())).getSubdivision(), setting))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPVP(EntityDamageByEntityEvent event){
        if(!(event.getEntity() instanceof Player)) return;
        Player attacker = null;
        if(event.getDamager() instanceof Player p){
            attacker = p;
        }else if(event.getDamager() instanceof Projectile pr && pr.getShooter() instanceof Player p){
            attacker = p;
        }
        if(attacker == null) return;
        if(attacker.hasPermission("neincraft.plots.bypass.pvp")) return;
        Optional<Plot> plot = getFromCache(event.getEntity().getChunk());
        if(plot.isEmpty() || !plot.get().resolveSettingsValue(plot.get().getChunkData(ChunkKey.fromChunk(event.getEntity().getChunk())).getSubdivision(), PlotSetting.ALLOW_PVP)){
            event.setCancelled(true);
            attacker.sendActionBar(Lang.PLOT_CANT_MODIFY.getComponent(attacker));
        }
    }

    @EventHandler
    public void onElementalDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof Player player){
            PlotSetting setting = null;
            if(event.getCause() == EntityDamageEvent.DamageCause.DROWNING){
                setting = PlotSetting.DROWNING_DAMAGE;
            }else if(event.getCause() == EntityDamageEvent.DamageCause.FALL){
                setting = PlotSetting.FALL_DAMAGE;
            }
            if(setting == null) return;
            Optional<Plot> plot = getFromCache(player.getChunk());
            if(plot.isPresent() && !plot.get().resolveSettingsValue(plot.get().getChunkData(ChunkKey.fromChunk(player.getChunk())).getSubdivision(), setting))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent event){
        if(!Tag.FIRE.isTagged(event.getSource().getType())) return;
        Optional<Plot> plot = getFromCache(event.getSource().getChunk());
        Optional<Plot> targetPlot = getFromCache(event.getBlock().getChunk());
        if(plot.isEmpty() || targetPlot.isEmpty() ||
                plot.get().resolveSettingsValue(plot.get().getChunkData(ChunkKey.fromChunk(event.getSource().getChunk())).getSubdivision(), PlotSetting.FIRE_SPREAD) ||
                targetPlot.get().resolveSettingsValue(targetPlot.get().getChunkData(ChunkKey.fromChunk(event.getBlock().getChunk())).getSubdivision(), PlotSetting.FIRE_SPREAD))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityUsePortal(EntityPortalEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerUsePortal(PlayerPortalEvent event){
        if(event.getPlayer().getWorld().getEnvironment() == World.Environment.THE_END && event.getFrom().getBlock().getType() != Material.NETHER_PORTAL) {
            Bukkit.getScheduler().runTask(NeincraftPlugin.getInstance(), () -> Bukkit.dispatchCommand(event.getPlayer(), "spawn"));
        }
        event.setCancelled(true);
    }

    @EventHandler
    public  void onBlockDispense(BlockPreDispenseEvent event){
        Block dispenser = event.getBlock();
        if(dispenser.getType() != Material.DISPENSER && dispenser.getType() != Material.DROPPER) return;
        if(dispenser.getBlockData() instanceof Directional directional){
            Block target = dispenser.getRelative(directional.getFacing());
            if(dispenser.getChunk() == target.getChunk()) return;
            if(!canPropagate(plotModule.getPlotAtChunk(ChunkKey.fromChunk(dispenser.getChunk())).orElse(null), plotModule.getPlotAtChunk(ChunkKey.fromChunk(target.getChunk())).orElse(null)))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDispensedProjectileHit(ProjectileHitEvent event){
        Projectile projectile = event.getEntity();
        if(projectile.getShooter() instanceof Player || projectile.getOrigin() == null) return;
        ChunkKey sourceChunk = ChunkKey.fromChunk(projectile.getOrigin().getChunk());
        ChunkKey targetChunk;
        if(event.getHitEntity() != null) {
            targetChunk = ChunkKey.fromChunk(event.getHitEntity().getChunk());
        }else if(event.getHitBlock() !=  null){
            targetChunk = ChunkKey.fromChunk(event.getHitBlock().getChunk());
        }else{
            targetChunk = ChunkKey.fromChunk(projectile.getChunk());
        }
        if(sourceChunk.equals(targetChunk)) return;
        if(!canPropagate(plotModule.getPlotAtChunk(sourceChunk).orElse(null), plotModule.getPlotAtChunk(targetChunk).orElse(null)))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBatchChanged(BlockBatchChangedEvent event){
        Block source = event.getBlock();
        Optional<Plot> sourcePlot = getFromCache(source.getChunk());
        event.getChangingBlocks().removeIf(targetBlock -> {
            Optional<Plot> targetPlot = getFromCache(targetBlock.getChunk());
            return !canPropagate(sourcePlot.orElse(null), targetPlot.orElse(null));
        });
    }

    //Mobgriefing
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event){
        if(event.getEntityType() != EntityType.ENDERMAN && event.getEntityType() != EntityType.ZOMBIE) return;
        if(plotModule.isPublicWorld(event.getBlock().getWorld()) || plotModule.getPlotAtChunk(ChunkKey.fromChunk(event.getBlock().getChunk())).isPresent())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if(event.getKeepInventory()) return;
        ChunkKey chunk = ChunkKey.fromChunk(event.getPlayer().getChunk());
        Optional<Plot> plot = plotModule.getPlotAtChunk(chunk);
        if(plot.isEmpty()) return;
        Plot p = plot.get();
        SubdivisionData subdivisionData = p.getChunkData(chunk).getSubdivision();
        if(subdivisionData == null) return;
        if(p.resolveSettingsValue(subdivisionData, PlotSetting.KEEP_INVENTORY) || p.resolveSettingsValue(subdivisionData, PlotSetting.ALLOW_PVP))
            event.setKeepInventory(true);
    }

}
