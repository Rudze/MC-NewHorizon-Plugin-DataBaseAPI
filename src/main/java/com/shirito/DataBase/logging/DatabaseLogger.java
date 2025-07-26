package com.shirito.DataBase.logging;

import com.shirito.DataBase.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced logging system for the DataBase plugin
 * Provides configurable logging with different levels and query logging
 * 
 * @author Shirito
 * @version 1.0
 */
public class DatabaseLogger {
    private final Logger bukkitLogger;
    private final ConfigManager config;
    private final String pluginName;
    
    public DatabaseLogger(JavaPlugin plugin, ConfigManager config) {
        this.bukkitLogger = plugin.getLogger();
        this.config = config;
        this.pluginName = plugin.getName();
    }
    
    /**
     * Log an info message
     * 
     * @param message Message to log
     */
    public void info(String message) {
        if (shouldLog(Level.INFO)) {
            bukkitLogger.info(formatMessage(message));
        }
    }
    
    /**
     * Log a warning message
     * 
     * @param message Message to log
     */
    public void warning(String message) {
        if (shouldLog(Level.WARNING)) {
            bukkitLogger.warning(formatMessage(message));
        }
    }
    
    /**
     * Log a warning message with exception
     * 
     * @param message Message to log
     * @param throwable Exception to log
     */
    public void warning(String message, Throwable throwable) {
        if (shouldLog(Level.WARNING)) {
            bukkitLogger.log(Level.WARNING, formatMessage(message), throwable);
        }
    }
    
    /**
     * Log a severe error message
     * 
     * @param message Message to log
     */
    public void severe(String message) {
        if (shouldLog(Level.SEVERE)) {
            bukkitLogger.severe(formatMessage(message));
        }
    }
    
    /**
     * Log a severe error message with exception
     * 
     * @param message Message to log
     * @param throwable Exception to log
     */
    public void severe(String message, Throwable throwable) {
        if (shouldLog(Level.SEVERE)) {
            bukkitLogger.log(Level.SEVERE, formatMessage(message), throwable);
        }
    }
    
    /**
     * Log a debug message (only if debug mode is enabled)
     * 
     * @param message Message to log
     */
    public void debug(String message) {
        if (config.isDebugMode() && shouldLog(Level.INFO)) {
            bukkitLogger.info(formatMessage("[DEBUG] " + message));
        }
    }
    
    /**
     * Log a debug message with exception (only if debug mode is enabled)
     * 
     * @param message Message to log
     * @param throwable Exception to log
     */
    public void debug(String message, Throwable throwable) {
        if (config.isDebugMode() && shouldLog(Level.INFO)) {
            bukkitLogger.log(Level.INFO, formatMessage("[DEBUG] " + message), throwable);
        }
    }
    
    /**
     * Log a database query (only if query logging is enabled)
     * 
     * @param query SQL query to log
     * @param parameters Query parameters
     */
    public void logQuery(String query, Object... parameters) {
        if (config.isQueryLogging() && config.isDebugMode()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[QUERY] ").append(query);
            
            if (parameters.length > 0) {
                sb.append(" [PARAMS: ");
                for (int i = 0; i < parameters.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(parameters[i]);
                }
                sb.append("]");
            }
            
            bukkitLogger.info(formatMessage(sb.toString()));
        }
    }
    
    /**
     * Log connection pool statistics
     * 
     * @param stats Pool statistics string
     */
    public void logPoolStats(String stats) {
        if (config.isDebugMode()) {
            debug("Connection Pool Stats: " + stats);
        }
    }
    
    /**
     * Log database operation timing
     * 
     * @param operation Operation name
     * @param duration Duration in milliseconds
     */
    public void logTiming(String operation, long duration) {
        if (config.isDebugMode()) {
            debug(String.format("Operation '%s' completed in %dms", operation, duration));
        }
    }
    
    /**
     * Log plugin startup information
     */
    public void logStartup() {
        info("=".repeat(50));
        info(pluginName + " Plugin Starting...");
        info("Version: " + getClass().getPackage().getImplementationVersion());
        info("Database Type: " + config.getDatabaseType());
        info("Debug Mode: " + (config.isDebugMode() ? "ENABLED" : "DISABLED"));
        info("Async Operations: " + (config.isAsyncOperations() ? "ENABLED" : "DISABLED"));
        info("=".repeat(50));
    }
    
    /**
     * Log plugin shutdown information
     */
    public void logShutdown() {
        info("=".repeat(50));
        info(pluginName + " Plugin Shutting Down...");
        info("Thank you for using " + pluginName + "!");
        info("=".repeat(50));
    }
    
    /**
     * Check if a message should be logged based on configuration
     * 
     * @param level Log level
     * @return true if message should be logged
     */
    private boolean shouldLog(Level level) {
        String configLevel = config.getLogLevel().toUpperCase();
        
        switch (configLevel) {
            case "DEBUG":
                return true; // Log everything
            case "INFO":
                return level.intValue() >= Level.INFO.intValue();
            case "WARN":
            case "WARNING":
                return level.intValue() >= Level.WARNING.intValue();
            case "ERROR":
            case "SEVERE":
                return level.intValue() >= Level.SEVERE.intValue();
            default:
                return level.intValue() >= Level.INFO.intValue();
        }
    }
    
    /**
     * Format a log message with plugin prefix
     * 
     * @param message Message to format
     * @return Formatted message
     */
    private String formatMessage(String message) {
        return message; // Bukkit logger already includes plugin name
    }
    
    /**
     * Get the underlying Bukkit logger
     * 
     * @return Bukkit logger instance
     */
    public Logger getBukkitLogger() {
        return bukkitLogger;
    }
}