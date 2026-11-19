package ru.winxboyz.reore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import ru.winxboyz.reore.managers.DatabaseManager;
import ru.winxboyz.reore.managers.PluginManager;
import ru.winxboyz.reore.tasks.TickResources;
import ru.winxboyz.reore.utils.ResourceData;
import ru.winxboyz.reore.listeners.BlockListener;

public class ReOre extends JavaPlugin {
    
    private String locale;

    private DatabaseManager databaseManager;
    private Map<String,String> messages = new HashMap<>();
    private List<ResourceData> resources;
    private ConcurrentHashMap<Location,ResourceData> registries = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Location,Long> cooldowns = new ConcurrentHashMap<>();
    private TickResources tasks;

    private ConfigurationSection localeSection,sqlSection,resourcesSection;
    private String dbName, tableName;

    public String LOCALE() {
        return locale;
    }
    public String TABLE_NAME() {
        return tableName;
    }
    public List<ResourceData> getResources() {
        return resources;
    }
    public ConcurrentHashMap<Location,ResourceData> getRegistries() {
        return registries;
    }
    public ConcurrentHashMap<Location,Long> getCooldowns() {
        return cooldowns;
    }
    public Map<String,String> getMessages() {
        return messages;
    }

    

    public void loadConfig() {
        locale = getConfig().getString("locale","en");
        
        resourcesSection = getConfig().getConfigurationSection("resources");
        sqlSection     = getConfig().getConfigurationSection("sql");
        localeSection  = getConfig().getConfigurationSection("locales." + locale);

        if(sqlSection == null) {
            getLogger().severe("No SQL connection credentials specified");
            return;
        }

        if(resourcesSection == null) {
            resources = List.of(
                new ResourceData(Material.COAL_ORE, 5 * 60 * 1000L),new ResourceData(Material.DEEPSLATE_COAL_ORE, 5 * 60 * 1000L),
                new ResourceData(Material.COPPER_ORE, 10 * 60 * 1000L),new ResourceData(Material.DEEPSLATE_COPPER_ORE, 10 * 60 * 1000L),
                new ResourceData(Material.LAPIS_ORE, 15 * 60 * 1000L),new ResourceData(Material.DEEPSLATE_LAPIS_ORE, 15 * 60 * 1000L),
                new ResourceData(Material.REDSTONE_ORE, 15 * 60 * 1000L),new ResourceData(Material.DEEPSLATE_REDSTONE_ORE, 15 * 60 * 1000L),
                new ResourceData(Material.IRON_ORE, 30 * 60 * 1000L),new ResourceData(Material.DEEPSLATE_IRON_ORE, 30 * 60 * 1000L),
                new ResourceData(Material.GOLD_ORE, 30 * 60 * 1000L),new ResourceData(Material.DEEPSLATE_GOLD_ORE, 30 * 60 * 1000L),
                new ResourceData(Material.DIAMOND_ORE, 60 * 60 * 1000L),new ResourceData(Material.DEEPSLATE_DIAMOND_ORE, 60 * 60 * 1000L),
                new ResourceData(Material.EMERALD_ORE, 60 * 60 * 1000L),new ResourceData(Material.DEEPSLATE_EMERALD_ORE, 60 * 60 * 1000L),
                
                new ResourceData(Material.NETHER_QUARTZ_ORE, 5 * 60 * 1000L),
                new ResourceData(Material.NETHER_GOLD_ORE, 5 * 60 * 1000L),
                new ResourceData(Material.ANCIENT_DEBRIS, 2 * 60 * 60 * 1000L));
            for(ResourceData data : resources) {
                getConfig().set("resources." + data.getResource() + ".cooldown", data.getCooldown());
                getConfig().set("resources." + data.getResource() + ".placeholder", data.getPlaceholder().getMaterial());
            }        
            saveConfig();
        } else {
            resources = new ArrayList<>();
            Set<String> keys = resourcesSection.getKeys(false);
            for(String key : keys) {
                Material res = Material.getMaterial(key);
                BlockData placeholder = Bukkit.createBlockData(resourcesSection.getString(key + ".placeholder", "minecraft:bedrock"));
                Long cooldown = resourcesSection.getLong(key + ".cooldown");

                resources.add(new ResourceData(res, cooldown, placeholder));
            }
        }

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
                for(Location loc : locs) {
                    ResourceData data = resources.stream().filter(
                        rd -> loc.getBlock().getType().equals(rd.getResource())
                    ).findFirst().orElse(null);
                    if(data == null) {
                        getLogger().warning("Could not find resource data for block at "
                        + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()
                        + " in world " + loc.getWorld().getName());
                        continue;
                    }
                    registries.put(loc, data);
                }
            });
        });
    }

    public void reload() {
        reloadConfig();
        tasks.cancelTicks(false);
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

        pluginManager.getCommandManager().initialize();

        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        
        tasks = new TickResources(registries, cooldowns);

        Bukkit.getScheduler().runTaskTimer(this, tasks::tick, 10L, 10L);
        getLogger().info(getName() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        tasks.cancelTicks(true);
        Bukkit.getScheduler().cancelTasks(this);
        databaseManager.close();
        getLogger().info(getName() + " has been disabled!");
    }
    
}
