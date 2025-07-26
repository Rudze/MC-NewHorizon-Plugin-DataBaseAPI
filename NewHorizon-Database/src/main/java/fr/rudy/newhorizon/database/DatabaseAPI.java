package fr.rudy.newhorizon.database;

import fr.rudy.newhorizon.database.managers.HomesManager;

import java.sql.Connection;

/**
 * Database API for other plugins to access database functionality
 */
public class DatabaseAPI {
    private static DatabaseAPI instance;
    private final DatabaseMain plugin;
    private final HomesManager homesManager;

    public DatabaseAPI(DatabaseMain plugin) {
        this.plugin = plugin;
        this.homesManager = new HomesManager();
        instance = this;
    }

    public static DatabaseAPI getInstance() {
        return instance;
    }

    /**
     * Get direct database connection (use with caution)
     * @return Database connection
     */
    public Connection getConnection() {
        return plugin.getDatabase();
    }

    /**
     * Get homes manager for player home operations
     * @return HomesManager instance
     */
    public HomesManager getHomesManager() {
        return homesManager;
    }

    /**
     * Check if database is available and connected
     * @return true if database is available
     */
    public boolean isAvailable() {
        try {
            return plugin.getDatabase() != null && !plugin.getDatabase().isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the database plugin instance
     * @return DatabaseMain instance
     */
    public DatabaseMain getPlugin() {
        return plugin;
    }

    /**
     * Execute a database update query (for economy plugin use)
     * @param query SQL query to execute
     * @param parameters Parameters for the query
     * @return true if successful
     */
    public boolean executeUpdate(String query, Object... parameters) {
        try (java.sql.PreparedStatement statement = getConnection().prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Execute a database query and return result (for economy plugin use)
     * @param query SQL query to execute
     * @param parameters Parameters for the query
     * @return ResultSet or null if failed
     */
    public java.sql.ResultSet executeQuery(String query, Object... parameters) {
        try {
            java.sql.PreparedStatement statement = getConnection().prepareStatement(query);
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            return statement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Execute a database statement (for schema modifications)
     * @param sql SQL statement to execute
     * @return true if successful
     */
    public boolean executeStatement(String sql) {
        try (java.sql.Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            // Ignore duplicate column errors for ALTER TABLE operations
            if (!e.getMessage().contains("duplicate column name")) {
                e.printStackTrace();
            }
            return false;
        }
    }
}