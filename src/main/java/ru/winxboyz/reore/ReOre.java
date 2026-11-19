package ru.winxboyz.reore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import ru.winxboyz.reore.managers.PluginManager;
import ru.winxboyz.reore.tasks.TickResources;
import ru.winxboyz.reore.utils.ResourceData;
import ru.winxboyz.reore.commands.ReOreCommand;
import ru.winxboyz.reore.listeners.BlockBreakListener;
import ru.winxboyz.reore.listeners.BlockDamageListener;

public class ReOre extends JavaPlugin {
    

    private final PluginManager pluginManager = PluginManager.getInstance();
    private String locale;

    private final Map<String,String> messages = new HashMap<>();
    private final List<ResourceData> resources = new ArrayList<>();
    private final ConcurrentHashMap<Location,ResourceData> registries = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Location,Long> cooldowns = new ConcurrentHashMap<>();

    private TickResources tasks;

    private String tableName;

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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

    public void reload() {
        reloadConfig();
        tasks.cancelTicks(false);
        pluginManager.getConfigManager().loadConfig();
    }


    @Override
    public void onEnable() {
        saveDefaultConfig();

        locale = getConfig().getString("locale","en");
        pluginManager.initialize(this);
        pluginManager.getConfigManager().loadConfig();
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(new ReOreCommand(this).initialize());
        });
        getServer().getPluginManager().registerEvents(new BlockBreakListener(pluginManager.getRestorationManager()), this);
        getServer().getPluginManager().registerEvents(new BlockDamageListener(pluginManager.getRestorationManager()), this);

        tasks = new TickResources(registries, cooldowns);

        Bukkit.getScheduler().runTaskTimer(this, tasks::tick, 10L, 10L);
        getLogger().info(getName() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        tasks.cancelTicks(true);
        Bukkit.getScheduler().cancelTasks(this);
        pluginManager.getDatabaseManager().close();
        getLogger().info(getName() + " has been disabled!");
    }
    
}
