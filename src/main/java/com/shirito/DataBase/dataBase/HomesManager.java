package com.shirito.DataBase.dataBase;

import com.shirito.DataBase.config.ConfigManager;
import com.shirito.DataBase.connection.ConnectionPool;
import com.shirito.DataBase.exceptions.ConnectionException;
import com.shirito.DataBase.logging.DatabaseLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

/**
 * Enhanced Homes Manager with dependency injection and proper resource management
 * Handles player home operations with connection pooling and comprehensive logging
 * 
 * @author Shirito
 * @version 2.0
 */
public class HomesManager {
    private final ConnectionPool connectionPool;
    private final DatabaseLogger logger;
    private final ConfigManager config;
    
    public HomesManager(ConnectionPool connectionPool, DatabaseLogger logger, ConfigManager config) {
        this.connectionPool = connectionPool;
        this.logger = logger;
        this.config = config;
    }
    
    /**
     * Get a database connection from the pool
     * 
     * @return Database connection
     * @throws ConnectionException if connection cannot be obtained
     */
    private Connection getConnection() throws ConnectionException {
        return connectionPool.getConnection();
    }
    
    /**
     * Return a connection to the pool
     * 
     * @param connection Connection to return
     */
    private void returnConnection(Connection connection) {
        if (connection != null) {
            connectionPool.returnConnection(connection);
        }
    }

    /**
     * Get player's home location
     * 
     * @param player Player UUID
     * @return Home location or null if not set
     */
    public Location getHome(UUID player) {
        if (player == null) {
            logger.warning("Attempted to get home for null player UUID");
            return null;
        }
        
        Connection connection = null;
        try {
            connection = getConnection();
            
            String query = "SELECT uuid, home_world, home_x, home_y, home_z, home_yaw, home_pitch " +
                          "FROM player_data WHERE uuid = ?";
            
            logger.logQuery(query, player.toString());
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, player.toString());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        logger.debug("No home found for player: " + player);
                        return null;
                    }

                    String worldName = resultSet.getString("home_world");
                    if (worldName == null) {
                        logger.debug("Player " + player + " has null home world");
                        return null;
                    }
                    
                    final Location homeLocation = new Location(
                            Bukkit.getWorld(worldName),
                            resultSet.getDouble("home_x"),
                            resultSet.getDouble("home_y"),
                            resultSet.getDouble("home_z"),
                            resultSet.getFloat("home_yaw"),
                            resultSet.getFloat("home_pitch")
                    );
                    
                    logger.debug("Retrieved home for player " + player + " at " + worldName);
                    return homeLocation;
                }
            }
        } catch (ConnectionException e) {
            logger.severe("Failed to get database connection for getHome", e);
        } catch (SQLException e) {
            logger.severe("Database error while getting home for player " + player, e);
        } finally {
            returnConnection(connection);
        }

        return null;
    }

    /**
     * Set player's home location
     * 
     * @param player Player UUID
     * @param home Home location to set
     * @return true if successful, false otherwise
     */
    public boolean setHome(UUID player, Location home) {
        if (player == null) {
            logger.warning("Attempted to set home for null player UUID");
            return false;
        }
        
        if (home == null || home.getWorld() == null) {
            logger.warning("Attempted to set null home or home with null world for player " + player);
            return false;
        }
        
        Connection connection = null;
        try {
            connection = getConnection();
            
            String query = "INSERT INTO player_data (uuid, home_world, home_x, home_y, home_z, home_yaw, home_pitch) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                          "ON CONFLICT(uuid) DO UPDATE SET " +
                          "home_world = excluded.home_world, " +
                          "home_x = excluded.home_x, " +
                          "home_y = excluded.home_y, " +
                          "home_z = excluded.home_z, " +
                          "home_yaw = excluded.home_yaw, " +
                          "home_pitch = excluded.home_pitch";
            
            logger.logQuery(query, player.toString(), home.getWorld().getName(), 
                           home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, player.toString());
                statement.setString(2, home.getWorld().getName());
                statement.setDouble(3, home.getX());
                statement.setDouble(4, home.getY());
                statement.setDouble(5, home.getZ());
                statement.setFloat(6, home.getYaw());
                statement.setFloat(7, home.getPitch());
                
                int rowsAffected = statement.executeUpdate();
                
                if (rowsAffected > 0) {
                    logger.debug("Successfully set home for player " + player + " at " + home.getWorld().getName());
                    return true;
                } else {
                    logger.warning("No rows affected when setting home for player " + player);
                    return false;
                }
            }
        } catch (ConnectionException e) {
            logger.severe("Failed to get database connection for setHome", e);
            return false;
        } catch (SQLException e) {
            logger.severe("Database error while setting home for player " + player, e);
            return false;
        } finally {
            returnConnection(connection);
        }
    }

    /**
     * Ensure player exists in the database with default values
     * 
     * @param player Player UUID
     * @return true if player was created or already exists, false on error
     */
    public boolean ensurePlayerExists(UUID player) {
        if (player == null) {
            logger.warning("Attempted to ensure existence for null player UUID");
            return false;
        }
        
        Connection connection = null;
        try {
            connection = getConnection();
            
            String query = "INSERT OR IGNORE INTO player_data (uuid, experience) VALUES (?, 0)";
            
            logger.logQuery(query, player.toString());
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, player.toString());
                int rowsAffected = statement.executeUpdate();
                
                if (rowsAffected > 0) {
                    logger.debug("Created new player record for " + player);
                } else {
                    logger.debug("Player record already exists for " + player);
                }
                
                return true;
            }
        } catch (ConnectionException e) {
            logger.severe("Failed to get database connection for ensurePlayerExists", e);
            return false;
        } catch (SQLException e) {
            logger.severe("Database error while ensuring player exists for " + player, e);
            return false;
        } finally {
            returnConnection(connection);
        }
    }
}