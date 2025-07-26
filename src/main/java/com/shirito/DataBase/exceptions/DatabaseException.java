package com.shirito.DataBase.exceptions;

/**
 * Base exception class for database-related errors
 * 
 * @author Shirito
 * @version 1.0
 */
public class DatabaseException extends Exception {
    
    /**
     * Constructs a new DatabaseException with the specified detail message.
     * 
     * @param message the detail message
     */
    public DatabaseException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new DatabaseException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new DatabaseException with the specified cause.
     * 
     * @param cause the cause of the exception
     */
    public DatabaseException(Throwable cause) {
        super(cause);
    }
}