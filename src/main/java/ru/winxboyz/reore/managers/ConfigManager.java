package ru.winxboyz.reore.managers;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;

import ru.winxboyz.reore.ReOre;
import ru.winxboyz.reore.utils.ResourceData;

public class ConfigManager {

    private ReOre plugin;

    private String dbName;

    private ConfigurationSection localeSection,sqlSection,resourcesSection;

    public void loadConfig() {
        
        plugin.setLocale(plugin.getConfig().getString("locale","en"));
        
        resourcesSection = plugin.getConfig().getConfigurationSection("resources");
        sqlSection     = plugin.getConfig().getConfigurationSection("sql");
        localeSection  = plugin.getConfig().getConfigurationSection("locales." + plugin.getLocale());

        if(sqlSection == null) {
            plugin.getLogger().severe("No SQL connection credentials specified");
            return;
        }

        if(resourcesSection == null) {
            plugin.getResources().addAll(List.of(
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
                new ResourceData(Material.ANCIENT_DEBRIS, 2 * 60 * 60 * 1000L)));
            for(ResourceData data : plugin.getResources()) {
                plugin.getConfig().set("resources." + data.getResource() + ".cooldown", data.getCooldown());
                plugin.getConfig().set("resources." + data.getResource() + ".placeholder", data.getPlaceholder().getMaterial());
            }        
            plugin.saveConfig();
        } else {
            plugin.getResources().clear();
            Set<String> keys = resourcesSection.getKeys(false);
            for(String key : keys) {
                Material res = Material.getMaterial(key);
                BlockData placeholder = Bukkit.createBlockData(resourcesSection.getString(key + ".placeholder", "minecraft:bedrock"));
                Long cooldown = resourcesSection.getLong(key + ".cooldown");

                plugin.getResources().add(new ResourceData(res, cooldown, placeholder));
            }
        }

        if(localeSection != null)
            for(String key : localeSection.getKeys(false))
                plugin.getMessages().put(key, localeSection.getString(key));
        plugin.setTableName(sqlSection.getString("locationsTable"));
        if(dbName == null || !dbName.equals(sqlSection.getString("database"))) {
            dbName = sqlSection.getString("database","reore");
            PluginManager.getInstance().getDatabaseManager().close();
            PluginManager.getInstance().getDatabaseManager().connect(dbName);
        }
        PluginManager.getInstance().getDatabaseManager().createTableLocations().thenAccept(res -> {
            PluginManager.getInstance().getDatabaseManager().fetchLocations().thenAccept(locs -> {
                for(Location loc : locs) {
                    ResourceData data = plugin.getResources().stream().filter(
                        rd -> loc.getBlock().getType().equals(rd.getResource())
                    ).findFirst().orElse(null);
                    if(data == null) {
                        plugin.getLogger().warning("Could not find resource data for block at "
                        + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()
                        + " in world " + loc.getWorld().getName());
                        continue;
                    }
                    plugin.getRegistries().put(loc, data);
                }
            });
        });
    }

    public ConfigManager(ReOre plugin) {
        this.plugin = plugin;
    }

}
