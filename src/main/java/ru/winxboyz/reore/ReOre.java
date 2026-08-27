package ru.winxboyz.reore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import ru.winxboyz.reore.managers.CommandManager;
import ru.winxboyz.reore.managers.PluginManager;
import ru.winxboyz.reore.utils.OreData;
import ru.winxboyz.reore.listeners.BlockListener;

public class ReOre extends JavaPlugin {
    
    private Map<Material,Long> defaultTimings = new HashMap<>();
    private List<World> registeredWorlds = new ArrayList<>();
    private ConcurrentHashMap<Location,OreData> ores = new ConcurrentHashMap<>();

    public ConcurrentHashMap<Location,OreData> getOres() {
        return ores;
    }
    public Map<Material,Long> getDefaultTimings() {
        return defaultTimings;
    }
    public List<World> getWorlds() {
        return registeredWorlds;
    }

    private void tickOres() {
        if(ores.isEmpty()) return;
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<Location,OreData>> iterator = ores.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Location,OreData> entry = iterator.next();
            Location loc = entry.getKey();
            OreData data = entry.getValue();
            if(data == OreData.EMPTY) continue;
            if(data.getTimeToRegenerate() <= currentTime
            && loc.getWorld().isChunkLoaded(loc.blockX() >> 4, loc.blockZ() >> 4)) {
                loc.getBlock().setType(data.getType());
                iterator.remove();
            }
        }
    }
    private void cancelTicks() {
        if(ores.isEmpty()) return;
        Iterator<Map.Entry<Location,OreData>> iterator = ores.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Location,OreData> entry = iterator.next();
            Location loc = entry.getKey();
            OreData data = entry.getValue();
            if(data == OreData.EMPTY) continue;
            
            loc.getBlock().setType(data.getType());
            iterator.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeOreLocations() {
        if(getConfig().isList("locations")) {
            List<Location> locations = (List<Location>) getConfig().getList("locations");
            for(Location loc : locations)
                ores.put(loc, OreData.EMPTY);
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        List<String> worldNames = getConfig().getStringList("worlds");
        if(worldNames.isEmpty()) {
            worldNames = Arrays.asList("world");        
            getConfig().set("worlds", worldNames);        
            saveConfig();
        }
        for(String worldName : worldNames)
            registeredWorlds.add(Bukkit.getWorld(worldName));

        ConfigurationSection timingsSection = getConfig().getConfigurationSection("timings");
        if(timingsSection == null) {
            defaultTimings = Map.ofEntries(
                Map.entry(Material.COAL_ORE, 5 * 60 * 1000L),Map.entry(Material.DEEPSLATE_COAL_ORE, 5 * 60 * 1000L),
                Map.entry(Material.COPPER_ORE, 10 * 60 * 1000L),Map.entry(Material.DEEPSLATE_COPPER_ORE, 10 * 60 * 1000L),
                Map.entry(Material.LAPIS_ORE, 15 * 60 * 1000L),Map.entry(Material.DEEPSLATE_LAPIS_ORE, 15 * 60 * 1000L),
                Map.entry(Material.REDSTONE_ORE, 15 * 60 * 1000L),Map.entry(Material.DEEPSLATE_REDSTONE_ORE, 15 * 60 * 1000L),
                Map.entry(Material.IRON_ORE, 30 * 60 * 1000L),Map.entry(Material.DEEPSLATE_IRON_ORE, 30 * 60 * 1000L),
                Map.entry(Material.GOLD_ORE, 30 * 60 * 1000L),Map.entry(Material.DEEPSLATE_GOLD_ORE, 30 * 60 * 1000L),
                Map.entry(Material.DIAMOND_ORE, 60 * 60 * 1000L),Map.entry(Material.DEEPSLATE_DIAMOND_ORE, 60 * 60 * 1000L),
                Map.entry(Material.EMERALD_ORE, 60 * 60 * 1000L),Map.entry(Material.DEEPSLATE_EMERALD_ORE, 60 * 60 * 1000L),
                
                Map.entry(Material.NETHER_QUARTZ_ORE, 5 * 60 * 1000L),
                Map.entry(Material.NETHER_GOLD_ORE, 5 * 60 * 1000L),
                Map.entry(Material.ANCIENT_DEBRIS, 2 * 60 * 60 * 1000L)
            );
            for(Map.Entry<Material,Long> timing : defaultTimings.entrySet()) {
                getConfig().set("timings."+timing.getKey().name(), timing.getValue());
            }        
            saveConfig();
        }

        
        initializeOreLocations();
        for(String key : timingsSection.getKeys(false)) {
            Material material = Material.valueOf(key.toUpperCase());
            long time = timingsSection.getLong(key);
            defaultTimings.put(material, time);
        }
        

        PluginManager.getInstance().initialize();
        CommandManager.getInstance().initialize(this);
        
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);

        
        Bukkit.getScheduler().runTaskTimer(this, this::tickOres, 10L, 10L);

        getLogger().info(getName() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        cancelTicks();
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info(getName() + " has been disabled!");
    }
    
}
