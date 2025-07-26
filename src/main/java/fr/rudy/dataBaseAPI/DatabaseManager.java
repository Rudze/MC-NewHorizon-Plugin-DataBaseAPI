package fr.rudy.dataBaseAPI;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private Connection connection;

    public void init(File dataFolder) {
        try {
            File dbFile = new File("database.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS example (id INTEGER PRIMARY KEY, name TEXT)");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}