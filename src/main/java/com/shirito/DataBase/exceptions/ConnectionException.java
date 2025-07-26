package com.shirito.DataBase.exceptions;

/**
 * Exception thrown when database connection issues occur
 * 
 * @author Shirito
 * @version 1.0
 */
public class ConnectionException extends DatabaseException {
    
    /**
     * Constructs a new ConnectionException with the specified detail message.
     * 
     * @param message the detail message
     */
    public ConnectionException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ConnectionException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new ConnectionException with the specified cause.
     * 
     * @param cause the cause of the exception
     */
    public ConnectionException(Throwable cause) {
        super(cause);
    }
}