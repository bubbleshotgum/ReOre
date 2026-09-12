package ru.winxboyz.reore.managers;

import ru.winxboyz.reore.ReOre;

public class PluginManager {
    private static PluginManager instance;
    private DatabaseManager dbManager;

    public DatabaseManager getDatabaseManager() { return dbManager; }
    
    private PluginManager() {}
    
    public static PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    public void initialize(ReOre plugin, String tableName) {
        dbManager = new DatabaseManager(plugin, tableName);
    }
}
