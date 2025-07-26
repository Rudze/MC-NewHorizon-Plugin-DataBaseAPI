package fr.rudy.databaseapi;

import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin {

    private static main instance;
    private DatabaseManager databaseManager;

    public static main get() {
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