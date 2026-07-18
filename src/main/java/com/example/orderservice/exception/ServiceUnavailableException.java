package com.example.orderservice.exception;

/**
 * Thrown when a service (like Inventory Service) is unavailable
 * 
 * Example: 
 * throw new ServiceUnavailableException("Inventory service is down after 3 retries");
 */
public class ServiceUnavailableException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public ServiceUnavailableException(String message) {
        super(message);
    }
    
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
