package fr.rudy.databaseapi;

import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseAPI extends JavaPlugin {

    private static DatabaseAPI instance;
    private DatabaseManager databaseManager;

    public static DatabaseAPI get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        databaseManager = new DatabaseManager();
        databaseManager.init(getDataFolder());
        getLogger().info("✅ DatabaseAPI activée !");
    }

    @Override
    public void onDisable() {
        databaseManager.close();
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}