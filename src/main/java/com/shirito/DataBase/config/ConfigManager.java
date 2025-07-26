package com.shirito.DataBase.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Configuration manager for the DataBase plugin
 * Provides centralized access to all configuration values
 * 
 * @author Shirito
 * @version 1.0
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Load or reload the configuration file
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    /**
     * Save the current configuration to file
     */
    public void saveConfig() {
        plugin.saveConfig();
    }
    
    // Database Configuration
    
    /**
     * Get the database type (sqlite, mysql, postgresql)
     * @return Database type
     */
    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }
    
    /**
     * Get SQLite database file path
     * @return Database file path
     */
    public String getSQLiteFile() {
        return config.getString("database.sqlite.file", "database.db");
    }
    
    /**
     * Check if WAL mode is enabled for SQLite
     * @return true if WAL mode is enabled
     */
    public boolean isSQLiteWalMode() {
        return config.getBoolean("database.sqlite.wal_mode", true);
    }
    
    /**
     * Get SQLite connection timeout
     * @return Connection timeout in seconds
     */
    public int getSQLiteTimeout() {
        return config.getInt("database.sqlite.timeout", 30);
    }
    
    /**
     * Get MySQL host
     * @return MySQL host
     */
    public String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }
    
    /**
     * Get MySQL port
     * @return MySQL port
     */
    public int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }
    
    /**
     * Get MySQL database name
     * @return MySQL database name
     */
    public String getMySQLDatabase() {
        return config.getString("database.mysql.database", "minecraft");
    }
    
    /**
     * Get MySQL username
     * @return MySQL username
     */
    public String getMySQLUsername() {
        return config.getString("database.mysql.username", "root");
    }
    
    /**
     * Get MySQL password
     * @return MySQL password
     */
    public String getMySQLPassword() {
        return config.getString("database.mysql.password", "");
    }
    
    /**
     * Check if MySQL SSL is enabled
     * @return true if SSL is enabled
     */
    public boolean isMySQLSSL() {
        return config.getBoolean("database.mysql.ssl", false);
    }
    
    // Connection Pool Configuration
    
    /**
     * Get maximum number of connections in pool
     * @return Maximum connections
     */
    public int getMaxConnections() {
        return config.getInt("database.pool.max_connections", 10);
    }
    
    /**
     * Get minimum number of connections in pool
     * @return Minimum connections
     */
    public int getMinConnections() {
        return config.getInt("database.pool.min_connections", 2);
    }
    
    /**
     * Get connection timeout in milliseconds
     * @return Connection timeout
     */
    public long getConnectionTimeout() {
        return config.getLong("database.pool.connection_timeout", 30000);
    }
    
    /**
     * Get maximum lifetime of connection in milliseconds
     * @return Maximum lifetime
     */
    public long getMaxLifetime() {
        return config.getLong("database.pool.max_lifetime", 1800000);
    }
    
    /**
     * Get idle timeout in milliseconds
     * @return Idle timeout
     */
    public long getIdleTimeout() {
        return config.getLong("database.pool.idle_timeout", 600000);
    }
    
    // Logging Configuration
    
    /**
     * Get logging level
     * @return Log level (DEBUG, INFO, WARN, ERROR)
     */
    public String getLogLevel() {
        return config.getString("logging.level", "INFO");
    }
    
    /**
     * Check if debug mode is enabled
     * @return true if debug mode is enabled
     */
    public boolean isDebugMode() {
        return config.getBoolean("logging.debug", false);
    }
    
    /**
     * Check if query logging is enabled
     * @return true if query logging is enabled
     */
    public boolean isQueryLogging() {
        return config.getBoolean("logging.log_queries", false);
    }
    
    // Performance Configuration
    
    /**
     * Check if async operations are enabled
     * @return true if async operations are enabled
     */
    public boolean isAsyncOperations() {
        return config.getBoolean("performance.async_operations", true);
    }
    
    /**
     * Get batch size for bulk operations
     * @return Batch size
     */
    public int getBatchSize() {
        return config.getInt("performance.batch_size", 100);
    }
    
    /**
     * Check if cache is enabled
     * @return true if cache is enabled
     */
    public boolean isCacheEnabled() {
        return config.getBoolean("performance.cache.enabled", true);
    }
    
    /**
     * Get cache size
     * @return Cache size (number of entries)
     */
    public int getCacheSize() {
        return config.getInt("performance.cache.size", 1000);
    }
    
    /**
     * Get cache expiration time in minutes
     * @return Cache expiration time
     */
    public int getCacheExpiration() {
        return config.getInt("performance.cache.expiration", 30);
    }
    
    // Feature Configuration
    
    /**
     * Check if auto migration is enabled
     * @return true if auto migration is enabled
     */
    public boolean isAutoMigrate() {
        return config.getBoolean("features.auto_migrate", true);
    }
    
    /**
     * Check if health checks are enabled
     * @return true if health checks are enabled
     */
    public boolean isHealthChecks() {
        return config.getBoolean("features.health_checks", true);
    }
    
    /**
     * Check if metrics collection is enabled
     * @return true if metrics are enabled
     */
    public boolean isMetrics() {
        return config.getBoolean("features.metrics", false);
    }
}