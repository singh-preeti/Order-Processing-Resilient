package com.order;

/**
 * Thrown when a service takes too long to respond
 * 
 * Example: 
 * throw new TimeoutException("Inventory service took more than 5 seconds to respond");
 */
public class TimeoutException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public TimeoutException(String message) {
        super(message);
    }
    
    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
