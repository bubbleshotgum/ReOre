package ru.winxboyz.reore.managers;

import ru.winxboyz.reore.ReOre;

public class PluginManager {
    private static PluginManager instance;

    private PluginManager() {}
    
    public static PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    private CommandManager commandManager;
    private ConfigManager configManager;
    private DatabaseManager dbManager;
    private RestorationManager restorationManager;

    public CommandManager getCommandManager() { return commandManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return dbManager; }
    public RestorationManager getRestorationManager() { return restorationManager; }

    public void initialize(ReOre plugin) {
        commandManager = new CommandManager(plugin);
        configManager = new ConfigManager(plugin);
        dbManager = new DatabaseManager(plugin);
        restorationManager = new RestorationManager(plugin);
    }
}
