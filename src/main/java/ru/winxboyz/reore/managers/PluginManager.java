package ru.winxboyz.reore.managers;

import ru.winxboyz.reore.ReOre;

public class PluginManager {
    private static PluginManager instance;

    private CommandManager commandManager;
    private DatabaseManager dbManager;

    public CommandManager getCommandManager() { return commandManager; }
    public DatabaseManager getDatabaseManager() { return dbManager; }
    
    private PluginManager() {}
    
    public static PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    public void initialize(ReOre plugin) {
        commandManager = new CommandManager(plugin);
        dbManager = new DatabaseManager(plugin);
    }
}
