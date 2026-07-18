package com.order.service;

import com.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * NotificationService sends notifications for orders
 * (In real world, this would call email/SMS provider)
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    /**
     * Send order confirmation notification
     * (In demo, just logs it)
     */
    public void sendOrderConfirmation(Order order) {
        logger.info("📧 Sending order confirmation for order: {}", order.getId());
        
        // In real application:
        // - Send email via SES/SendGrid
        // - Send SMS via Twilio
        // - For now, just log
    }
}
