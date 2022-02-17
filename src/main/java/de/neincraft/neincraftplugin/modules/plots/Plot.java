package de.neincraft.neincraftplugin.modules.plots;

import de.neincraft.neincraftplugin.modules.plots.dto.*;
import de.neincraft.neincraftplugin.modules.plots.dto.embeddable.*;
import de.neincraft.neincraftplugin.modules.plots.repository.PlotRepository;
import de.neincraft.neincraftplugin.modules.plots.util.PlotBoundingBox;
import de.neincraft.neincraftplugin.modules.plots.util.PlotPermission;
import de.neincraft.neincraftplugin.modules.plots.util.PlotSetting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Plot {

    private static final Set<String> protectedGroups = Set.of("owners", "everyone");
    private static final Set<String> protectedSubdivisions = Set.of("main");

    private long id;
    private PlotData plotData;
    private boolean needsMarkerUpdate;

    private HashMap<ChunkKey, ChunkData> chunks = new HashMap<>();
    private PlotBoundingBox boundingBox;

    public static Plot createNewPlot(String name, ChunkKey startChunk, @Nullable UUID owner, LocationData home){
        PlotData data = new PlotData();
        data.setWorldName(startChunk.getWorld());
        data.setHome(home);
        data.setName(name);
        data.setOwner(owner);
        SubdivisionData subdivision = new SubdivisionData(data, new ArrayList<>(), "main");
        ChunkData chunkData = new ChunkData(startChunk, data, subdivision);
        PlotMemberGroup members = new PlotMemberGroup(new GroupId(data, "members"), new ArrayList<>(), null);
        members.setGroupPermissions(PermissionFlag.getMemberDefaults(members, subdivision));
        PlotMemberGroup owners = new PlotMemberGroup(new GroupId(data, "owners"), owner != null ? new ArrayList<>(List.of(owner)) : new ArrayList<>(), null);
        owners.setGroupPermissions(PermissionFlag.getOwnerDefaults(owners, subdivision));
        PlotMemberGroup everyone = new PlotMemberGroup(new GroupId(data, "everyone"), new ArrayList<>(), null);
        everyone.setGroupPermissions(PermissionFlag.getEveryoneDefault(everyone, subdivision));
        data.setGroups(new ArrayList<>(List.of(members, owners, everyone)));
        subdivision.setSettings(PlotSettingsEntry.createDefaults(subdivision));
        data.setChunks(new ArrayList<>(List.of(chunkData)));
        data.setSubdivisions(new ArrayList<>(List.of(subdivision)));
        Plot plot = new Plot(data);
        plot.persist();
        plot.refreshData();
        plot.refreshChunks();
        return plot;
    }

    public static Plot getPlotFromData(PlotData data){
        Plot plot = new Plot(data);
        plot.refreshChunks();
        return plot;
    }

    private Plot(PlotData data){
        this.plotData = data;
        this.id = data.getId();
        updateDefaults();
    }

    public PlotData getPlotData(){
        return plotData;
    }

    public SubdivisionData getSubdivision(String name){
        for(SubdivisionData subdivision : plotData.getSubdivisions()){
            if(subdivision.getName().equalsIgnoreCase(name)){
                return subdivision;
            }
        }
        return null;
    }

    public boolean createSubdivision(String name){
        if(getSubdivision(name) != null)
            return false;
        SubdivisionData subdivision = new SubdivisionData(plotData, Collections.emptyList(), name);
        plotData.getSubdivisions().add(subdivision);
        return true;
    }

    public boolean deleteSubdivision(SubdivisionData subdivisionData){
        if(subdivisionData == null || protectedSubdivisions.contains(subdivisionData.getName()))
            return false;
        SubdivisionData mainSubdivision = getSubdivision("main");
        plotData.getChunks().stream().filter(chunkData -> chunkData.getSubdivision() == subdivisionData).forEach(chunkData -> chunkData.setSubdivision(mainSubdivision));
        return plotData.getSubdivisions().remove(subdivisionData);
    }

    public ChunkData getChunkData(ChunkKey chunk){
        if(!plotData.getWorldName().equals(chunk.getWorld()))
            return null;
        if(!boundingBox.isInside(chunk.getX(), chunk.getZ()))
            return null;
        return chunks.get(chunk);
    }

    public boolean hasChunk(ChunkKey chunk){
        return getChunkData(chunk) != null;
    }

    public boolean addChunk(ChunkKey chunk){
        if(hasChunk(chunk))
            return false;
        ChunkData newChunk = new ChunkData(chunk, plotData, getSubdivision("main"));
        plotData.getChunks().add(newChunk);
        refreshChunks();
        return true;
    }

    public boolean removeChunk(ChunkKey chunk){
        ChunkData data = getChunkData(chunk);
        if(data == null || plotData.getChunks().size() <= 1)
            return false;
        plotData.getChunks().remove(data);
        refreshChunks();
        return true;
    }

    public void setHome(Location loc){
        plotData.setHome(LocationData.fromBukkitlocation(loc));
    }

    public Location getHome(){
        World w = Bukkit.getWorld(plotData.getWorldName());
        if(w == null)
            return null;
        LocationData locationData = plotData.getHome();
        return new Location(w, locationData.getX(), locationData.getY(), locationData.getZ(), locationData.getYaw(), locationData.getPitch());
    }

    public PlotMemberGroup getGroup(String name){
        for(PlotMemberGroup group : plotData.getGroups()){
            if(group.getGroupId().getGroupName().equalsIgnoreCase(name))
                return group;
        }
        return null;
    }

    public boolean createGroup(String name){
        if(getGroup(name) != null)
            return false;
        PlotMemberGroup group = new PlotMemberGroup(new GroupId(plotData, name), Collections.emptyList(), Collections.emptyList());
        plotData.getGroups().add(group);
        return true;
    }

    public boolean deleteGroup(PlotMemberGroup group){
        if(group == null || protectedGroups.contains(group.getGroupId().getGroupName()))
            return false;
        return plotData.getGroups().remove(group);
    }

    public PlotMemberGroup getPlayerGroup(UUID player){
        for(PlotMemberGroup group : plotData.getGroups()){
            for(UUID member : group.getMembers()){
                if(member.equals(player))
                    return group;
            }
        }
        return getGroup("everyone");
    }

    public void setPlayerGroup(UUID player, PlotMemberGroup group){
        getPlayerGroup(player).getMembers().remove(player);
        if(group == null || group.getGroupId().getGroupName().equals("everyone")) return;
        group.getMembers().add(player);
    }

    private PlotSettingsEntry getSettingsEntry(SubdivisionData subdivisionData, PlotSetting setting){
        if(setting == null || subdivisionData == null)
            return null;
        for(PlotSettingsEntry entry : subdivisionData.getSettings()){
            if(entry.getSettingId().getSetting() == setting)
                return entry;
        }
        return null;
    }

    public PlotSettingsEntry getSettingsEntry(String subdivision, PlotSetting setting){
        return getSettingsEntry(getSubdivision(subdivision), setting);
    }

    public boolean resolveSettingsValue(String subdivision, PlotSetting setting, Consumer<String> subdivisionCallback){
        PlotSettingsEntry entry = getSettingsEntry(subdivision, setting);
        if(entry == null && !subdivision.equalsIgnoreCase("main"))
            return resolveSettingsValue("main", setting, subdivisionCallback);
        if(subdivisionCallback != null)
            subdivisionCallback.accept(entry != null ? subdivision : null);
        return entry != null ? entry.getValue() : setting.getDefaultValue();
    }

    public boolean resolveSettingsValue(String subdivision, PlotSetting setting){
        return resolveSettingsValue(subdivision, setting, null);
    }

    public void unsetSetting(SubdivisionData subdivisionData, PlotSetting setting){
        if(subdivisionData == null)
            return;
        PlotSettingsEntry entry;
        while((entry = getSettingsEntry(subdivisionData, setting)) != null){
            subdivisionData.getSettings().remove(entry);
        }
    }

    public void unsetSetting(String subdivision, PlotSetting setting){
        unsetSetting(getSubdivision(subdivision), setting);
    }

    public void setSetting(SubdivisionData subdivisionData, PlotSetting setting, boolean value){
        if(subdivisionData == null)
            return;
        unsetSetting(subdivisionData, setting);
        PlotSettingsEntry settingsEntry = new PlotSettingsEntry(new SettingId(subdivisionData, setting), value);
        subdivisionData.getSettings().add(settingsEntry);
    }

    public void setSetting(String subdivision, PlotSetting setting, boolean value){
        setSetting(getSubdivision(subdivision), setting, value);
    }

    public PermissionFlag getPermissionData(SubdivisionData subdivisionData, PlotMemberGroup groupData, PlotPermission permission){
        if(subdivisionData == null || groupData == null || permission == null)
            return null;
        for(PermissionFlag permissionFlag : groupData.getGroupPermissions()){
            if(permissionFlag.getPermissionId().getSubdivision().getSubdivisionId() == subdivisionData.getSubdivisionId()
                    && permissionFlag.getPermissionId().getPermissionKey() == permission){
                return permissionFlag;
            }
        }
        return null;
    }

    public void setPermission(SubdivisionData subdivisionData, PlotMemberGroup group, PlotPermission permission, boolean value){
        PermissionFlag permissionFlag;
        if((permissionFlag = getPermissionData(subdivisionData, group, permission)) == null) {
            permissionFlag = new PermissionFlag(new PermissionId(group, subdivisionData, permission), value);
            group.getGroupPermissions().add(permissionFlag);
        }else{
            permissionFlag.setValue(value);
        }
    }

    public void unsetPermission(SubdivisionData subdivisionData, PlotMemberGroup group, PlotPermission permission){
        PermissionFlag permissionFlag = getPermissionData(subdivisionData, group, permission);
        if(permissionFlag != null){
            group.getGroupPermissions().remove(permissionFlag);
        }
    }

    public boolean resolvePermission(SubdivisionData subdivision, PlotMemberGroup group, PlotPermission permission, BiConsumer<SubdivisionData, PlotMemberGroup> definedIn){
        BiConsumer<SubdivisionData, PlotMemberGroup> callback = definedIn != null ? definedIn : (a,b) -> {};
        PermissionFlag permissionData = getPermissionData(subdivision, group, permission);
        if(permissionData != null) {
            callback.accept(subdivision, group);
            return permissionData.getValue();
        }

        PlotMemberGroup everyone = getGroup("everyone");
        permissionData = getPermissionData(subdivision, everyone, permission);
        if(permissionData != null) {
            callback.accept(subdivision, everyone);
            return permissionData.getValue();
        }

        SubdivisionData main = getSubdivision("main");
        permissionData = getPermissionData(main, group, permission);
        if(permissionData != null) {
            callback.accept(main, group);
            return permissionData.getValue();
        }

        permissionData = getPermissionData(main, everyone, permission);
        if(permissionData != null) {
            callback.accept(main, everyone);
            return permissionData.getValue();
        }
        callback.accept(null, null);
        return permission.getPublicDefault();
    }

    public boolean resolvePermission(SubdivisionData subdivision, PlotMemberGroup group, PlotPermission permission){
        return resolvePermission(subdivision, group, permission, null);
    }

    public boolean isServerPlot(){
        return plotData.getOwner() == null;
    }

    public void refreshData(){
        PlotRepository repository = PlotRepository.getRepository();
        if(repository != null) {
            plotData = repository.findById(this.id);
            repository.close();
        }
    }

    public void persist(){
        PlotRepository repository = PlotRepository.getRepository();
        if(repository != null) {
            repository.persist(plotData);
            id = plotData.getId();
            repository.close();
        }
    }

    public void updateDefaults(){
        SubdivisionData main = getSubdivision("main");
        PlotMemberGroup owners = getGroup("owners");
        PlotMemberGroup everyone = getGroup("everyone");
        PlotMemberGroup members = getGroup("members");
        owners.getGroupPermissions().addAll(PermissionFlag.getOwnerDefaults(owners, main).stream().filter(
                newFlag -> owners.getGroupPermissions().stream().noneMatch(
                        oldFlag -> oldFlag.getPermissionId().getPermissionKey().equals(newFlag.getPermissionId().getPermissionKey())
                )
        ).collect(Collectors.toSet()));
        everyone.getGroupPermissions().addAll(PermissionFlag.getEveryoneDefault(everyone, main).stream().filter(
                newFlag -> everyone.getGroupPermissions().stream().noneMatch(
                        oldFlag -> oldFlag.getPermissionId().getPermissionKey().equals(newFlag.getPermissionId().getPermissionKey())
                )
        ).collect(Collectors.toSet()));

        if(members != null){
            members.getGroupPermissions().addAll(PermissionFlag.getMemberDefaults(members, main).stream().filter(
                    newFlag -> members.getGroupPermissions().stream().noneMatch(
                            oldFlag -> oldFlag.getPermissionId().getPermissionKey().equals(newFlag.getPermissionId().getPermissionKey())
                    )
            ).collect(Collectors.toSet()));
        }

        main.getSettings().addAll(PlotSettingsEntry.createDefaults(main).stream().filter(newSetting ->
                main.getSettings().stream().noneMatch(oldSetting -> oldSetting.getSettingId().getSetting().equals(newSetting.getSettingId().getSetting()))
        ).collect(Collectors.toSet()));

    }

    public void refreshChunks(){
        chunks.clear();
        boundingBox = null;
        if(plotData.getChunks().size() <= 0)
            return;
        int minX, maxX, minZ, maxZ;
        ChunkKey first = plotData.getChunks().stream().findFirst().get().getId();
        minX = first.getX();
        maxX = first.getX();
        minZ = first.getZ();
        maxZ = first.getZ();
        for(ChunkData cd : plotData.getChunks()){
            ChunkKey id = cd.getId();
            chunks.put(id, cd);
            if(id.getX() < minX)
                minX = id.getX();
            if(id.getX() > maxX)
                maxX = id.getX();
            if(id.getZ() < minZ)
                minZ = id.getZ();
            if(id.getZ() > maxZ)
                maxZ = id.getZ();
        }
        boundingBox = new PlotBoundingBox(minX, maxX, minZ, maxZ);
        needsMarkerUpdate = true;
    }

    public void resetMarkerUpdateStatus(){
        needsMarkerUpdate = false;
    }

    public boolean needsMarkerUpdate(){
        return needsMarkerUpdate;
    }

}
