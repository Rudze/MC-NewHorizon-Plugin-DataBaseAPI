package com.shirito.DataBase.schema;

import com.shirito.DataBase.exceptions.DatabaseException;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database schema creation and migrations
 * Handles all table creation and database structure updates
 * 
 * @author Shirito
 * @version 1.0
 */
public class SchemaManager {
    private final Logger logger;
    private final List<String> tableCreationQueries;
    
    public SchemaManager(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        this.tableCreationQueries = new ArrayList<>();
        initializeTableQueries();
    }
    
    /**
     * Initialize all table creation queries
     */
    private void initializeTableQueries() {
        // Player data table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS player_data (" +
            "uuid VARCHAR(36) PRIMARY KEY, " +
            "experience INT DEFAULT 0, " +
            "home_world VARCHAR(64), " +
            "home_x DOUBLE, home_y DOUBLE, home_z DOUBLE, " +
            "home_yaw FLOAT, home_pitch FLOAT, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        );
        
        // World spawns table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS world_spawns (" +
            "world_name VARCHAR(64) PRIMARY KEY, " +
            "x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, " +
            "yaw FLOAT DEFAULT 0, pitch FLOAT DEFAULT 0, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        );
        
        // Warps table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS warps (" +
            "name TEXT PRIMARY KEY, " +
            "world TEXT NOT NULL, " +
            "x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, " +
            "yaw FLOAT NOT NULL, pitch FLOAT NOT NULL, " +
            "creator_uuid VARCHAR(36), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        );
        
        // Cities table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS cities (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "owner_uuid TEXT NOT NULL, " +
            "city_name TEXT UNIQUE NOT NULL, " +
            "world TEXT NOT NULL, " +
            "x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, " +
            "yaw FLOAT DEFAULT 0, pitch FLOAT DEFAULT 0, " +
            "likes INTEGER DEFAULT 0, " +
            "liked_by TEXT DEFAULT '', " +
            "members TEXT DEFAULT '', " +
            "banner TEXT, " +
            "bank_balance DOUBLE DEFAULT 0.0, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        );
        
        // City claims table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS city_claims (" +
            "chunk_x INTEGER NOT NULL, " +
            "chunk_z INTEGER NOT NULL, " +
            "world TEXT NOT NULL, " +
            "city_id INTEGER NOT NULL, " +
            "claimed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "PRIMARY KEY (chunk_x, chunk_z, world), " +
            "FOREIGN KEY (city_id) REFERENCES cities(id) ON DELETE CASCADE)"
        );
        
        // Core spawn table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS core_spawn (" +
            "id INTEGER PRIMARY KEY CHECK (id = 0), " +
            "world TEXT NOT NULL, " +
            "x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, " +
            "yaw FLOAT NOT NULL, pitch FLOAT NOT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        );
        
        // Analyzer blocks table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS analyzer_blocks (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "world TEXT NOT NULL, " +
            "x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, " +
            "data TEXT DEFAULT NULL, " +
            "owner_uuid VARCHAR(36), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "UNIQUE(world, x, y, z))"
        );
        
        // Incubator blocks table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS incubator_blocks (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "world TEXT NOT NULL, " +
            "x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, " +
            "data TEXT DEFAULT NULL, " +
            "owner_uuid VARCHAR(36), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "UNIQUE(world, x, y, z))"
        );
        
        // Egg blocks table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS egg_blocks (" +
            "world TEXT NOT NULL, " +
            "x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, " +
            "stage INT DEFAULT 0, " +
            "owner_uuid VARCHAR(36), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "PRIMARY KEY (world, x, y, z))"
        );
        
        // Friends table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS friends (" +
            "player_uuid TEXT NOT NULL, " +
            "friend_uuid TEXT NOT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "PRIMARY KEY (player_uuid, friend_uuid))"
        );
        
        // Friend requests table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS friend_requests (" +
            "sender_uuid TEXT NOT NULL, " +
            "receiver_uuid TEXT NOT NULL, " +
            "message TEXT, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "PRIMARY KEY (sender_uuid, receiver_uuid))"
        );
        
        // Dialogue progress table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS player_dialogues (" +
            "uuid TEXT NOT NULL, " +
            "dialogue_id TEXT NOT NULL, " +
            "step INT NOT NULL, " +
            "completed BOOLEAN DEFAULT FALSE, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "PRIMARY KEY (uuid, dialogue_id))"
        );
        
        // Votes table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS votes (" +
            "uuid TEXT PRIMARY KEY, " +
            "pending_votes INT DEFAULT 0, " +
            "total_votes INT DEFAULT 0, " +
            "last_vote TIMESTAMP, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        );
        
        // Cinematics table
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS cinematics (" +
            "uuid TEXT NOT NULL, " +
            "cinematic_id TEXT NOT NULL, " +
            "played BOOLEAN DEFAULT FALSE, " +
            "play_count INT DEFAULT 0, " +
            "first_played TIMESTAMP, " +
            "last_played TIMESTAMP, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "PRIMARY KEY (uuid, cinematic_id))"
        );
        
        // Schema version table for migrations
        tableCreationQueries.add(
            "CREATE TABLE IF NOT EXISTS schema_version (" +
            "version INTEGER PRIMARY KEY, " +
            "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "description TEXT)"
        );
    }
    
    /**
     * Create all database tables
     * 
     * @param connection Database connection
     * @throws DatabaseException if table creation fails
     */
    public void createTables(Connection connection) throws DatabaseException {
        logger.info("Creating database tables...");
        
        try (Statement statement = connection.createStatement()) {
            int tablesCreated = 0;
            
            for (String query : tableCreationQueries) {
                try {
                    statement.executeUpdate(query);
                    tablesCreated++;
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Failed to create table with query: " + query, e);
                    throw new DatabaseException("Failed to create database table", e);
                }
            }
            
            logger.info("Successfully created " + tablesCreated + " database tables");
            
            // Initialize schema version if not exists
            initializeSchemaVersion(connection);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create database tables", e);
            throw new DatabaseException("Failed to create database tables", e);
        }
    }
    
    /**
     * Initialize schema version tracking
     * 
     * @param connection Database connection
     * @throws SQLException if initialization fails
     */
    private void initializeSchemaVersion(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Insert initial schema version if not exists
            statement.executeUpdate(
                "INSERT OR IGNORE INTO schema_version (version, description) " +
                "VALUES (1, 'Initial schema creation')"
            );
        }
    }
    
    /**
     * Get current schema version
     * 
     * @param connection Database connection
     * @return Current schema version
     * @throws SQLException if query fails
     */
    public int getCurrentSchemaVersion(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             var resultSet = statement.executeQuery("SELECT MAX(version) as version FROM schema_version")) {
            
            if (resultSet.next()) {
                return resultSet.getInt("version");
            }
            return 0;
        }
    }
    
    /**
     * Check if all required tables exist
     * 
     * @param connection Database connection
     * @return true if all tables exist
     */
    public boolean tablesExist(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            // Check if at least the player_data table exists
            var resultSet = statement.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='player_data'"
            );
            return resultSet.next();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to check table existence", e);
            return false;
        }
    }
}