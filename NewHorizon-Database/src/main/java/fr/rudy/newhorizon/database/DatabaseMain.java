package fr.rudy.newhorizon.database;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMain extends JavaPlugin {
    private static DatabaseMain instance;
    private Connection database;
    private DatabaseAPI databaseAPI;

    @Override
    public void onEnable() {
        instance = this;
        setupDatabase();
        databaseAPI = new DatabaseAPI(this);
        getLogger().info("✅ NewHorizon Database plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (database != null) {
            try {
                database.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        getLogger().info("❌ NewHorizon Database plugin disabled!");
    }

    private void setupDatabase() {
        try {
            database = DriverManager.getConnection("jdbc:sqlite:database.db");

            try (Statement statement = database.createStatement()) {
                // Player data table
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS newhorizon_player_data (" +
                                "uuid VARCHAR(36) PRIMARY KEY, " +
                                "experience INT DEFAULT 0, " +
                                "home_world VARCHAR(64), " +
                                "home_x DOUBLE, home_y DOUBLE, home_z DOUBLE, " +
                                "home_yaw FLOAT, home_pitch FLOAT)"
                );

                // World spawns table
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS newhorizon_world_spawns (" +
                                "world_name VARCHAR(64) PRIMARY KEY, " +
                                "x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT)");

                // Warps table
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS newhorizon_warps (" +
                        "name TEXT PRIMARY KEY, " +
                        "world TEXT NOT NULL, " +
                        "x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, " +
                        "yaw FLOAT NOT NULL, pitch FLOAT NOT NULL)");

                // Cities table
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS newhorizon_cities (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "owner_uuid TEXT NOT NULL, " +
                        "city_name TEXT UNIQUE NOT NULL, " +
                        "world TEXT NOT NULL, x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, " +
                        "yaw FLOAT, pitch FLOAT, likes INTEGER DEFAULT 0, " +
                        "liked_by TEXT DEFAULT '', members TEXT DEFAULT '', " +
                        "banner TEXT, bank_balance DOUBLE DEFAULT 0.0)");

                // City claims table
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS newhorizon_city_claims (" +
                        "chunk_x INTEGER NOT NULL, chunk_z INTEGER NOT NULL, " +
                        "world TEXT NOT NULL, city_id INTEGER NOT NULL, " +
                        "PRIMARY KEY (chunk_x, chunk_z, world))");

                // Core spawn table
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS newhorizon_core_spawn (" +
                        "id INTEGER PRIMARY KEY CHECK (id = 0), world TEXT NOT NULL, " +
                        "x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, " +
                        "yaw FLOAT NOT NULL, pitch FLOAT NOT NULL)");

                // Analyzer blocks table
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS newhorizon_analyzer_blocks (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, world TEXT NOT NULL, " +
                        "x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, data TEXT DEFAULT NULL)");

                // Incubator blocks table
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS newhorizon_incubator_blocks (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, world TEXT NOT NULL, " +
                        "x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, data TEXT DEFAULT NULL)");

                // Egg blocks table
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS newhorizon_egg_blocks (" +
                        "world TEXT NOT NULL, x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, " +
                        "stage INT DEFAULT 0, PRIMARY KEY (world, x, y, z))");

                // Friends table
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS newhorizon_friends (" +
                        "player_uuid TEXT NOT NULL, friend_uuid TEXT NOT NULL, " +
                        "PRIMARY KEY (player_uuid, friend_uuid))");

                // Friend requests table
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS newhorizon_friend_requests (" +
                        "sender_uuid TEXT NOT NULL, receiver_uuid TEXT NOT NULL, " +
                        "PRIMARY KEY (sender_uuid, receiver_uuid))");

                // Dialogue progress table
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS newhorizon_player_dialogues (" +
                        "uuid TEXT NOT NULL, dialogue_id TEXT NOT NULL, step INT NOT NULL, " +
                        "PRIMARY KEY (uuid, dialogue_id))");

                // Votes table
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS newhorizon_votes (" +
                        "uuid TEXT PRIMARY KEY, pending_votes INT DEFAULT 0)");

                // Cinematics table
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS newhorizon_cinematics (" +
                        "uuid TEXT NOT NULL, cinematic_id TEXT NOT NULL, played BOOLEAN DEFAULT FALSE, " +
                        "PRIMARY KEY (uuid, cinematic_id))");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public static DatabaseMain getInstance() {
        return instance;
    }

    public Connection getDatabase() {
        return database;
    }

    public DatabaseAPI getDatabaseAPI() {
        return databaseAPI;
    }
}