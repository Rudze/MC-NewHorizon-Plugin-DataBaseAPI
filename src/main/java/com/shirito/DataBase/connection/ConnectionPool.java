package com.shirito.DataBase.connection;

import com.shirito.DataBase.config.ConfigManager;
import com.shirito.DataBase.exceptions.ConnectionException;
import com.shirito.DataBase.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database connection pool manager
 * Provides efficient connection pooling with configurable parameters
 * 
 * @author Shirito
 * @version 1.0
 */
public class ConnectionPool {
    private final Logger logger;
    private final ConfigManager config;
    private final BlockingQueue<Connection> availableConnections;
    private final AtomicInteger activeConnections;
    private final AtomicBoolean isShutdown;
    private final String connectionUrl;
    
    public ConnectionPool(Logger logger, ConfigManager config) throws DatabaseException {
        this.logger = logger;
        this.config = config;
        this.availableConnections = new ArrayBlockingQueue<>(config.getMaxConnections());
        this.activeConnections = new AtomicInteger(0);
        this.isShutdown = new AtomicBoolean(false);
        this.connectionUrl = buildConnectionUrl();
        
        initializePool();
    }
    
    /**
     * Build the database connection URL based on configuration
     * 
     * @return Connection URL
     * @throws DatabaseException if configuration is invalid
     */
    private String buildConnectionUrl() throws DatabaseException {
        String dbType = config.getDatabaseType().toLowerCase();
        
        switch (dbType) {
            case "sqlite":
                String dbFile = config.getSQLiteFile();
                String url = "jdbc:sqlite:" + dbFile;
                
                // Add SQLite specific parameters
                if (config.isSQLiteWalMode()) {
                    url += "?journal_mode=WAL";
                }
                
                return url;
                
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC",
                    config.getMySQLHost(),
                    config.getMySQLPort(),
                    config.getMySQLDatabase(),
                    config.isMySQLSSL());
                
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s",
                    config.getMySQLHost(), // Using same config for now
                    config.getMySQLPort(),
                    config.getMySQLDatabase());
                
            default:
                throw new DatabaseException("Unsupported database type: " + dbType);
        }
    }
    
    /**
     * Initialize the connection pool with minimum connections
     * 
     * @throws DatabaseException if initialization fails
     */
    private void initializePool() throws DatabaseException {
        logger.info("Initializing connection pool...");
        
        try {
            // Create minimum number of connections
            for (int i = 0; i < config.getMinConnections(); i++) {
                Connection connection = createNewConnection();
                availableConnections.offer(connection);
                activeConnections.incrementAndGet();
            }
            
            logger.info("Connection pool initialized with " + config.getMinConnections() + " connections");
            
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize connection pool", e);
        }
    }
    
    /**
     * Create a new database connection
     * 
     * @return New database connection
     * @throws SQLException if connection creation fails
     */
    private Connection createNewConnection() throws SQLException {
        String dbType = config.getDatabaseType().toLowerCase();
        
        if ("mysql".equals(dbType)) {
            return DriverManager.getConnection(
                connectionUrl,
                config.getMySQLUsername(),
                config.getMySQLPassword()
            );
        } else {
            return DriverManager.getConnection(connectionUrl);
        }
    }
    
    /**
     * Get a connection from the pool
     * 
     * @return Database connection
     * @throws ConnectionException if no connection is available
     */
    public Connection getConnection() throws ConnectionException {
        if (isShutdown.get()) {
            throw new ConnectionException("Connection pool is shutdown");
        }
        
        try {
            // Try to get an available connection
            Connection connection = availableConnections.poll(
                config.getConnectionTimeout(), 
                TimeUnit.MILLISECONDS
            );
            
            if (connection == null) {
                // Try to create a new connection if under max limit
                if (activeConnections.get() < config.getMaxConnections()) {
                    connection = createNewConnection();
                    activeConnections.incrementAndGet();
                    
                    if (config.isDebugMode()) {
                        logger.info("Created new connection. Active connections: " + activeConnections.get());
                    }
                } else {
                    throw new ConnectionException("Connection pool exhausted. Max connections: " + config.getMaxConnections());
                }
            }
            
            // Validate connection
            if (connection == null || connection.isClosed()) {
                // Remove invalid connection and try again
                if (connection != null) {
                    activeConnections.decrementAndGet();
                }
                return getConnection(); // Recursive call
            }
            
            return connection;
            
        } catch (SQLException e) {
            throw new ConnectionException("Failed to get database connection", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectionException("Connection request interrupted", e);
        }
    }
    
    /**
     * Return a connection to the pool
     * 
     * @param connection Connection to return
     */
    public void returnConnection(Connection connection) {
        if (connection == null || isShutdown.get()) {
            return;
        }
        
        try {
            if (!connection.isClosed()) {
                // Reset connection state
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                }
                
                // Return to pool if there's space
                if (!availableConnections.offer(connection)) {
                    // Pool is full, close the connection
                    connection.close();
                    activeConnections.decrementAndGet();
                    
                    if (config.isDebugMode()) {
                        logger.info("Closed excess connection. Active connections: " + activeConnections.get());
                    }
                }
            } else {
                // Connection is closed, decrement counter
                activeConnections.decrementAndGet();
            }
            
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error returning connection to pool", e);
            activeConnections.decrementAndGet();
        }
    }
    
    /**
     * Get the number of active connections
     * 
     * @return Number of active connections
     */
    public int getActiveConnections() {
        return activeConnections.get();
    }
    
    /**
     * Get the number of available connections in the pool
     * 
     * @return Number of available connections
     */
    public int getAvailableConnections() {
        return availableConnections.size();
    }
    
    /**
     * Check if the connection pool is healthy
     * 
     * @return true if pool is healthy
     */
    public boolean isHealthy() {
        return !isShutdown.get() && activeConnections.get() > 0;
    }
    
    /**
     * Shutdown the connection pool
     */
    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            logger.info("Shutting down connection pool...");
            
            // Close all available connections
            Connection connection;
            while ((connection = availableConnections.poll()) != null) {
                try {
                    connection.close();
                    activeConnections.decrementAndGet();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error closing connection during shutdown", e);
                }
            }
            
            logger.info("Connection pool shutdown complete. Closed " + activeConnections.get() + " connections");
        }
    }
    
    /**
     * Get pool statistics as a formatted string
     * 
     * @return Pool statistics
     */
    public String getPoolStats() {
        return String.format(
            "ConnectionPool[active=%d, available=%d, max=%d, shutdown=%s]",
            activeConnections.get(),
            availableConnections.size(),
            config.getMaxConnections(),
            isShutdown.get()
        );
    }
}