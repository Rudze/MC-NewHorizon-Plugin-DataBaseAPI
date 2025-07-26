package com.shirito.DataBase.dataBase;

import com.shirito.DataBase.config.ConfigManager;
import com.shirito.DataBase.connection.ConnectionPool;
import com.shirito.DataBase.exceptions.DatabaseException;
import com.shirito.DataBase.logging.DatabaseLogger;
import com.shirito.DataBase.schema.SchemaManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main plugin class for the DataBase plugin
 * Provides centralized database management with modern architecture
 * 
 * Features:
 * - Configuration management
 * - Connection pooling
 * - Schema management and migrations
 * - Async operations
 * - Comprehensive logging
 * - Health monitoring
 * 
 * @author Shirito
 * @version 2.0
 */
public final class DataBase extends JavaPlugin {
    
    // Singleton instance
    private static DataBase instance;
    
    // Core components
    private ConfigManager configManager;
    private DatabaseLogger logger;
    private SchemaManager schemaManager;
    private ConnectionPool connectionPool;
    private DatabaseAPI databaseAPI;
    
    // Async operations
    private ExecutorService asyncExecutor;
    
    // Health monitoring
    private BukkitRunnable healthCheckTask;
    
    // Plugin state
    private boolean isInitialized = false;
    
    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        
        try {
            // Set singleton instance
            instance = this;
            
            // Initialize core components in order
            initializeConfiguration();
            initializeLogging();
            initializeAsyncExecutor();
            initializeDatabase();
            initializeAPI();
            initializeHealthMonitoring();
            
            // Mark as initialized
            isInitialized = true;
            
            // Log successful startup
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ DataBase plugin enabled successfully in " + duration + "ms!");
            logger.logStartup();
            
        } catch (Exception e) {
            getLogger().severe("❌ Failed to enable DataBase plugin: " + e.getMessage());
            e.printStackTrace();
            
            // Cleanup and disable
            cleanup();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        if (logger != null) {
            logger.logShutdown();
        }
        
        cleanup();
        
        if (logger != null) {
            logger.info("❌ DataBase plugin disabled!");
        } else {
            getLogger().info("❌ DataBase plugin disabled!");
        }
    }
    
    /**
     * Initialize configuration management
     */
    private void initializeConfiguration() {
        configManager = new ConfigManager(this);
        getLogger().info("Configuration loaded successfully");
    }
    
    /**
     * Initialize enhanced logging system
     */
    private void initializeLogging() {
        logger = new DatabaseLogger(this, configManager);
        logger.info("Enhanced logging system initialized");
    }
    
    /**
     * Initialize async executor for database operations
     */
    private void initializeAsyncExecutor() {
        if (configManager.isAsyncOperations()) {
            asyncExecutor = Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
                r -> {
                    Thread thread = new Thread(r, "DataBase-Async-" + System.currentTimeMillis());
                    thread.setDaemon(true);
                    return thread;
                }
            );
            logger.info("Async executor initialized with thread pool");
        } else {
            logger.info("Async operations disabled in configuration");
        }
    }
    
    /**
     * Initialize database components
     */
    private void initializeDatabase() throws DatabaseException {
        logger.info("Initializing database components...");
        
        // Initialize schema manager
        schemaManager = new SchemaManager(this);
        logger.debug("Schema manager initialized");
        
        // Initialize connection pool
        connectionPool = new ConnectionPool(logger.getBukkitLogger(), configManager);
        logger.info("Connection pool initialized: " + connectionPool.getPoolStats());
        
        // Create database schema
        try (Connection connection = connectionPool.getConnection()) {
            schemaManager.createTables(connection);
            
            int schemaVersion = schemaManager.getCurrentSchemaVersion(connection);
            logger.info("Database schema initialized (version " + schemaVersion + ")");
            
            connectionPool.returnConnection(connection);
        } catch (Exception e) {
            throw new DatabaseException("Failed to initialize database schema", e);
        }
    }
    
    /**
     * Initialize database API
     */
    private void initializeAPI() {
        databaseAPI = new DatabaseAPI(this, connectionPool, logger, configManager);
        logger.info("Database API initialized");
    }
    
    /**
     * Initialize health monitoring
     */
    private void initializeHealthMonitoring() {
        if (configManager.isHealthChecks()) {
            healthCheckTask = new BukkitRunnable() {
                @Override
                public void run() {
                    performHealthCheck();
                }
            };
            
            // Run health check every 5 minutes
            healthCheckTask.runTaskTimerAsynchronously(this, 20L * 60 * 5, 20L * 60 * 5);
            logger.info("Health monitoring initialized (5-minute intervals)");
        }
    }
    
    /**
     * Perform health check on database components
     */
    private void performHealthCheck() {
        try {
            boolean poolHealthy = connectionPool.isHealthy();
            
            if (!poolHealthy) {
                logger.warning("Health check failed: Connection pool is unhealthy");
                return;
            }
            
            // Test database connectivity
            try (Connection connection = connectionPool.getConnection()) {
                boolean tablesExist = schemaManager.tablesExist(connection);
                
                if (!tablesExist) {
                    logger.warning("Health check failed: Database tables missing");
                    connectionPool.returnConnection(connection);
                    return;
                }
                
                connectionPool.returnConnection(connection);
            }
            
            logger.debug("Health check passed - all systems operational");
            logger.logPoolStats(connectionPool.getPoolStats());
            
        } catch (Exception e) {
            logger.warning("Health check failed with exception", e);
        }
    }
    
    /**
     * Execute a task asynchronously if async operations are enabled
     * 
     * @param task Task to execute
     * @return CompletableFuture for the task
     */
    public CompletableFuture<Void> executeAsync(Runnable task) {
        if (asyncExecutor != null && configManager.isAsyncOperations()) {
            return CompletableFuture.runAsync(task, asyncExecutor);
        } else {
            // Execute synchronously
            task.run();
            return CompletableFuture.completedFuture(null);
        }
    }
    
    /**
     * Execute a task asynchronously with result if async operations are enabled
     * 
     * @param task Task to execute
     * @param <T> Result type
     * @return CompletableFuture for the task result
     */
    public <T> CompletableFuture<T> executeAsync(java.util.concurrent.Callable<T> task) {
        if (asyncExecutor != null && configManager.isAsyncOperations()) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return task.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, asyncExecutor);
        } else {
            // Execute synchronously
            try {
                return CompletableFuture.completedFuture(task.call());
            } catch (Exception e) {
                CompletableFuture<T> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        }
    }
    
    /**
     * Cleanup all resources
     */
    private void cleanup() {
        // Cancel health check task
        if (healthCheckTask != null && !healthCheckTask.isCancelled()) {
            healthCheckTask.cancel();
            healthCheckTask = null;
        }
        
        // Shutdown async executor
        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            asyncExecutor = null;
        }
        
        // Shutdown connection pool
        if (connectionPool != null) {
            connectionPool.shutdown();
            connectionPool = null;
        }
        
        // Clear references
        databaseAPI = null;
        schemaManager = null;
        configManager = null;
        logger = null;
        isInitialized = false;
    }
    
    // Public API methods
    
    /**
     * Get the singleton instance
     * 
     * @return DataBase instance
     */
    public static DataBase getInstance() {
        return instance;
    }
    
    /**
     * Get the configuration manager
     * 
     * @return ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Get the database logger
     * 
     * @return DatabaseLogger instance
     */
    public DatabaseLogger getDatabaseLogger() {
        return logger;
    }
    
    /**
     * Get the schema manager
     * 
     * @return SchemaManager instance
     */
    public SchemaManager getSchemaManager() {
        return schemaManager;
    }
    
    /**
     * Get the connection pool
     * 
     * @return ConnectionPool instance
     */
    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }
    
    /**
     * Get the database API
     * 
     * @return DatabaseAPI instance
     */
    public DatabaseAPI getDatabaseAPI() {
        return databaseAPI;
    }
    
    /**
     * Get a database connection (legacy method for backward compatibility)
     * 
     * @return Database connection
     * @deprecated Use getConnectionPool().getConnection() instead
     */
    @Deprecated
    public Connection getDatabase() {
        try {
            return connectionPool.getConnection();
        } catch (Exception e) {
            logger.severe("Failed to get database connection", e);
            return null;
        }
    }
    
    /**
     * Check if the plugin is fully initialized
     * 
     * @return true if initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * Reload the plugin configuration
     */
    public void reloadConfiguration() {
        if (configManager != null) {
            configManager.loadConfig();
            logger.info("Configuration reloaded successfully");
        }
    }
}
