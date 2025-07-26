package com.shirito.DataBase.dataBase;

import com.shirito.DataBase.config.ConfigManager;
import com.shirito.DataBase.connection.ConnectionPool;
import com.shirito.DataBase.exceptions.ConnectionException;
import com.shirito.DataBase.logging.DatabaseLogger;

import java.sql.Connection;

/**
 * Enhanced Database API for other plugins to access database functionality
 * Provides modern, efficient database operations with connection pooling
 * 
 * @author Shirito
 * @version 2.0
 */
public class DatabaseAPI {
    private static DatabaseAPI instance;
    private final DataBase plugin;
    private final ConnectionPool connectionPool;
    private final DatabaseLogger logger;
    private final ConfigManager config;
    private final HomesManager homesManager;

    public DatabaseAPI(DataBase plugin, ConnectionPool connectionPool, DatabaseLogger logger, ConfigManager config) {
        this.plugin = plugin;
        this.connectionPool = connectionPool;
        this.logger = logger;
        this.config = config;
        this.homesManager = new HomesManager(connectionPool, logger, config);
        instance = this;
    }

    public static DatabaseAPI getInstance() {
        return instance;
    }

    /**
     * Get direct database connection from pool (use with caution)
     * Remember to return the connection using returnConnection() when done
     * 
     * @return Database connection
     * @throws ConnectionException if connection cannot be obtained
     */
    public Connection getConnection() throws ConnectionException {
        return connectionPool.getConnection();
    }
    
    /**
     * Return a connection to the pool
     * 
     * @param connection Connection to return
     */
    public void returnConnection(Connection connection) {
        if (connection != null) {
            connectionPool.returnConnection(connection);
        }
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
            return connectionPool != null && connectionPool.isHealthy();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the database plugin instance
     * @return DataBase instance
     */
    public DataBase getPlugin() {
        return plugin;
    }

    /**
     * Execute a database update query (for economy plugin use)
     * @param query SQL query to execute
     * @param parameters Parameters for the query
     * @return true if successful
     */
    public boolean executeUpdate(String query, Object... parameters) {
        if (query == null || query.trim().isEmpty()) {
            logger.warning("Attempted to execute null or empty query");
            return false;
        }
        
        Connection connection = null;
        try {
            connection = getConnection();
            logger.logQuery(query, parameters);
            
            try (java.sql.PreparedStatement statement = connection.prepareStatement(query)) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i + 1, parameters[i]);
                }
                
                int rowsAffected = statement.executeUpdate();
                logger.debug("Query executed successfully, " + rowsAffected + " rows affected");
                return true;
            }
        } catch (ConnectionException e) {
            logger.severe("Failed to get database connection for executeUpdate", e);
            return false;
        } catch (Exception e) {
            logger.severe("Error executing update query: " + query, e);
            return false;
        } finally {
            returnConnection(connection);
        }
    }


    /**
     * Execute a database query with automatic resource management
     * @param query SQL query to execute
     * @param callback Callback to process the ResultSet
     * @param parameters Parameters for the query
     * @return true if query executed successfully
     */
    public boolean executeQueryWithCallback(String query, ResultSetCallback callback, Object... parameters) {
        if (query == null || query.trim().isEmpty()) {
            logger.warning("Attempted to execute null or empty query");
            return false;
        }
        
        if (callback == null) {
            logger.warning("Attempted to execute query with null callback");
            return false;
        }
        
        Connection connection = null;
        try {
            connection = getConnection();
            logger.logQuery(query, parameters);
            
            try (java.sql.PreparedStatement statement = connection.prepareStatement(query)) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i + 1, parameters[i]);
                }
                
                try (java.sql.ResultSet resultSet = statement.executeQuery()) {
                    callback.process(resultSet);
                    logger.debug("Query with callback executed successfully");
                    return true;
                }
            }
        } catch (ConnectionException e) {
            logger.severe("Failed to get database connection for executeQueryWithCallback", e);
            return false;
        } catch (Exception e) {
            logger.severe("Error executing query with callback: " + query, e);
            return false;
        } finally {
            returnConnection(connection);
        }
    }

    /**
     * Callback interface for processing ResultSet with automatic resource management
     */
    public interface ResultSetCallback {
        void process(java.sql.ResultSet resultSet) throws java.sql.SQLException;
    }

    /**
     * Execute a database statement (for schema modifications)
     * @param sql SQL statement to execute
     * @return true if successful
     */
    public boolean executeStatement(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            logger.warning("Attempted to execute null or empty SQL statement");
            return false;
        }
        
        Connection connection = null;
        try {
            connection = getConnection();
            logger.logQuery(sql);
            
            try (java.sql.Statement statement = connection.createStatement()) {
                int rowsAffected = statement.executeUpdate(sql);
                logger.debug("Statement executed successfully, " + rowsAffected + " rows affected");
                return true;
            }
        } catch (ConnectionException e) {
            logger.severe("Failed to get database connection for executeStatement", e);
            return false;
        } catch (Exception e) {
            // Ignore duplicate column errors for ALTER TABLE operations
            if (e.getMessage() != null && e.getMessage().contains("duplicate column name")) {
                logger.debug("Ignoring duplicate column error for ALTER TABLE operation");
                return true;
            } else {
                logger.severe("Error executing SQL statement: " + sql, e);
                return false;
            }
        } finally {
            returnConnection(connection);
        }
    }
}