package ru.winxboyz.reore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import ru.winxboyz.reore.managers.CommandManager;
import ru.winxboyz.reore.managers.DatabaseManager;
import ru.winxboyz.reore.managers.PluginManager;
import ru.winxboyz.reore.utils.OreData;
import ru.winxboyz.reore.listeners.BlockListener;

public class ReOre extends JavaPlugin {
    
    private String locale;

    private DatabaseManager databaseManager;
    private Map<String,String> messages = new HashMap<>();
    private Map<Material,Long> defaultTimings = new HashMap<>();
    private ConcurrentHashMap<Location,OreData> ores = new ConcurrentHashMap<>();

    private ConfigurationSection localeSection,sqlSection,timingsSection;
    private String dbName, tableName;

    public String LOCALE() {
        return locale;
    }
    public String TABLE_NAME() {
        return tableName;
    }
    public ConcurrentHashMap<Location,OreData> getOres() {
        return ores;
    }
    public Map<Material,Long> getDefaultTimings() {
        return defaultTimings;
    }
    public Map<String,String> getMessages() {
        return messages;
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
                data = OreData.EMPTY;
            }
        }
    }
    private void cancelTicks(boolean shouldEmpty) {
        if(ores.isEmpty()) return;
        Iterator<Map.Entry<Location,OreData>> iterator = ores.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Location,OreData> entry = iterator.next();
            Location loc = entry.getKey();
            OreData data = entry.getValue();
            if(data == OreData.EMPTY) continue;
            
            loc.getBlock().setType(data.getType());
            if(shouldEmpty)
                iterator.remove();
            else
                entry.setValue(OreData.EMPTY);
        }
    }

    public void loadConfig() {
        // defaultTimings = new HashMap<>();

        locale = getConfig().getString("locale","en");
        
        timingsSection = getConfig().getConfigurationSection("timings");
        sqlSection     = getConfig().getConfigurationSection("sql");
        localeSection  = getConfig().getConfigurationSection("locales." + locale);

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
        Set<String> materials = timingsSection.getKeys(false);
        for(String key : materials) {
            Material material = Material.valueOf(key.toUpperCase());
            long time = timingsSection.getLong(key);
            defaultTimings.put(material, time);
        }

        for(Material key : defaultTimings.keySet())
            if(!materials.contains(key.name()))
                defaultTimings.remove(key);

        if(localeSection != null)
            for(String key : localeSection.getKeys(false))
                messages.put(key, localeSection.getString(key));
        tableName = sqlSection.getString("locationsTable");
        if(dbName == null || !dbName.equals(sqlSection.getString("database"))) {
            dbName = sqlSection.getString("database","reore");
            databaseManager.close();
            databaseManager.connect(dbName);
        }
        databaseManager.createTableLocations().thenAccept(res -> {
            databaseManager.fetchLocations().thenAccept(locs -> {
                for(Location loc : locs)
                    ores.put(loc, OreData.EMPTY);
            });
        });
    }

    public void reload() {
        reloadConfig();
        cancelTicks(false);
        loadConfig();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        locale = getConfig().getString("locale","en");
        PluginManager pluginManager = PluginManager.getInstance();
        pluginManager.initialize(this);
        databaseManager = pluginManager.getDatabaseManager();
        loadConfig();

        CommandManager.getInstance().initialize(this);

        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        
        Bukkit.getScheduler().runTaskTimer(this, this::tickOres, 10L, 10L);
        getLogger().info(getName() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        cancelTicks(true);
        Bukkit.getScheduler().cancelTasks(this);
        databaseManager.close();
        getLogger().info(getName() + " has been disabled!");
    }
    
}
