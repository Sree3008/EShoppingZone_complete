package com.eshoppingzone.notification.consumer;

import com.eshoppingzone.notification.config.RabbitMQConfig;
import com.eshoppingzone.notification.dto.DeliveryEvent;
import com.eshoppingzone.notification.dto.OrderEvent;
import com.eshoppingzone.notification.dto.PaymentEvent;
import com.eshoppingzone.notification.dto.ProductEvent;
import com.eshoppingzone.notification.dto.UserEvent;
import com.eshoppingzone.notification.dto.NotificationDto;
import com.eshoppingzone.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);
    private static final Long DEFAULT_ADMIN_USER_ID = 1L;

    private final NotificationService notificationService;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    public NotificationEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    private void processNotification(Long recipientUserId, String recipientRole, String actorId, String actorRole, Object entityId, String eventType, String title, String message, String notificationType) {
        processNotification(recipientUserId, recipientRole, actorId, actorRole, entityId, eventType, title, message, notificationType, null);
    }

    private void processNotification(Long recipientUserId, String recipientRole, String actorId, String actorRole, Object entityId, String eventType, String title, String message, String notificationType, String statusContext) {
        if (recipientUserId == null) {
            log.warn("Target user could not be identified for event: {}. Routing aborted.", eventType);
            return;
        }

        String eventId = notificationType + "-" + (entityId != null ? entityId : "0") + "-" + recipientUserId;
        if (statusContext != null && !statusContext.isEmpty()) {
            eventId += "-" + statusContext;
        }
        String recipientLabel = (recipientRole != null ? recipientRole : "USER") + "#" + recipientUserId;

        NotificationDto dto = notificationService.createNotification(recipientUserId, recipientRole, title, message, notificationType, eventId);

        boolean wsSent = false;
        if (messagingTemplate != null && dto != null) {
            try {
                messagingTemplate.convertAndSendToUser(String.valueOf(recipientUserId), "/queue/notifications", dto);
                wsSent = true;
            } catch (Exception e) {
                log.warn("Failed to send WebSocket notification to user {}: {}", recipientUserId, e.getMessage());
            }
        }

        log.info("\n========================================\n" +
                "NOTIFICATION ROUTING\n" +
                "========================================\n" +
                "Event: {}\n" +
                "Actor: {}\n" +
                "Actor Role: {}\n" +
                "Business Reference: {}\n" +
                "Message: {}\n" +
                "----------------------------------------\n" +
                "Recipient: {}\n" +
                "Recipient Role: {}\n" +
                "Recipient User ID: {}\n" +
                "----------------------------------------\n" +
                "Database Saved: true\n" +
                "WebSocket Sent: {}\n" +
                "========================================",
                eventType,
                actorId != null ? actorId : "SYSTEM",
                actorRole != null ? actorRole : "SYSTEM",
                entityId != null ? entityId : "N/A",
                message,
                recipientLabel,
                recipientRole != null ? recipientRole : "UNKNOWN",
                recipientUserId,
                wsSent);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_USER_NOTIFICATIONS)
    public void handleUserRegistration(UserEvent event) {
        if (event == null) {
            log.warn("Invalid user notification event.");
            return;
        }

        String eventType = event.getEventType() != null ? event.getEventType() : "USER_REGISTRATION";
        String actor = event.getUsername() != null ? event.getUsername() : "User#" + event.getUserId();
        String role = event.getRole() != null ? event.getRole() : "CUSTOMER";
        String title = "Welcome to EShoppingZone!";
        String message = "Hello " + (event.getFullName() != null ? event.getFullName() : (event.getUsername() != null ? event.getUsername() : "User")) +
                ", your account has been successfully created with role " + role + ".";

        processNotification(event.getUserId(), role, actor, role, event.getUserId(), eventType, title, message, "USER_REGISTRATION");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_NOTIFICATIONS)
    public void handleOrderEvent(OrderEvent event) {
        if (event == null) {
            log.warn("Invalid order notification event.");
            return;
        }

        String eventType = event.getEventType() != null ? event.getEventType() : "ORDER_EVENT";
        Long orderId = event.getOrderId();
        Long customerId = event.getCustomerId();
        Long merchantId = event.getMerchantId();
        String eventUpper = eventType.toUpperCase();

        // 1. CUSTOMER PLACES ORDER -> Recipient: MERCHANT
        if ("ORDER_CREATED".equalsIgnoreCase(eventType) || "ORDER_CONFIRMED".equalsIgnoreCase(eventType)) {
            String title = "New Order Received #" + orderId;
            String message = "You have received a new order #" + orderId + " for $" + (event.getTotalAmount() != null ? event.getTotalAmount() : "0.00") + ".";
            String actorId = customerId != null ? "Customer#" + customerId : "CUSTOMER";
            processNotification(merchantId, "MERCHANT", actorId, "CUSTOMER", orderId, eventType, title, message, "MERCHANT_ORDER_RECEIVED");
            return;
        }

        // 2. MERCHANT PACKS ORDER -> Recipient: ADMIN
        if ("ORDER_PROCESSING".equalsIgnoreCase(eventType) || "READY_FOR_DELIVERY".equalsIgnoreCase(eventType)
                || "ORDER_PACKED".equalsIgnoreCase(eventType) || "PACKED".equalsIgnoreCase(eventType)) {
            String title = "Order Packed #" + orderId;
            String message = "Order #" + orderId + " packed and ready.";
            String actorId = merchantId != null ? "Merchant#" + merchantId : "MERCHANT";
            processNotification(DEFAULT_ADMIN_USER_ID, "ADMIN", actorId, "MERCHANT", orderId, eventType, title, message, "ORDER_PACKED");
            return;
        }

        // 5. REFUND / RETURN REQUEST -> Recipient: ADMIN
        if ("RETURN_REQUESTED".equals(eventUpper) || "REFUND_REQUESTED".equals(eventUpper)) {
            String title = "Refund Request Received #" + orderId;
            String message = "Refund requested for Order #" + orderId + ".";
            String actorId = customerId != null ? "Customer#" + customerId : "CUSTOMER";
            processNotification(DEFAULT_ADMIN_USER_ID, "ADMIN", actorId, "CUSTOMER", orderId, eventType, title, message, "REFUND_REQUESTED");
            return;
        }

        // RETURN / REFUND APPROVAL -> Recipient: CUSTOMER
        if ("RETURN_APPROVED".equals(eventUpper) || "REFUND_APPROVED".equals(eventUpper)) {
            String title = "Return Request Approved #" + orderId;
            String message = "Your return request for order #" + orderId + " has been approved.";
            String actorId = "Admin#" + DEFAULT_ADMIN_USER_ID;
            processNotification(customerId, "CUSTOMER", actorId, "ADMIN", orderId, eventType, title, message, "RETURN_APPROVED");
            return;
        }

        // RETURN / REFUND REJECTION -> Recipient: CUSTOMER
        if ("RETURN_REJECTED".equals(eventUpper) || "REFUND_REJECTED".equals(eventUpper)) {
            String title = "Return Request Rejected #" + orderId;
            String message = "Your return request for order #" + orderId + " has been rejected.";
            String actorId = "Admin#" + DEFAULT_ADMIN_USER_ID;
            processNotification(customerId, "CUSTOMER", actorId, "ADMIN", orderId, eventType, title, message, "RETURN_REJECTED");
            return;
        }

        // ORDER CANCELLED
        if ("ORDER_CANCELLED".equalsIgnoreCase(eventType)) {
            if (merchantId != null && !merchantId.equals(customerId)) {
                String title = "Order Cancelled #" + orderId;
                String message = "Order #" + orderId + " was cancelled.";
                String actorId = customerId != null ? "Customer#" + customerId : "CUSTOMER";
                processNotification(merchantId, "MERCHANT", actorId, "CUSTOMER", orderId, eventType, title, message, "ORDER_CANCELLED");
            }
            
            String title = "Order Cancelled #" + orderId;
            String message = "Your order #" + orderId + " has been cancelled. Any reserved amount will be refunded.";
            processNotification(customerId, "CUSTOMER", "SYSTEM", "SYSTEM", orderId, eventType, title, message, "ORDER_CANCELLED");
            
            return;
        }

        // Default status update for customer
        String title = "Order Update #" + orderId;
        String message = "Your order #" + orderId + " status is now " + event.getStatus() + ".";
        processNotification(customerId, "CUSTOMER", "SYSTEM", "SYSTEM", orderId, eventType, title, message, "ORDER_STATUS");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_NOTIFICATIONS)
    public void handlePaymentEvent(PaymentEvent event) {
        if (event == null) {
            log.warn("Invalid payment notification event.");
            return;
        }

        String eventType = event.getEventType() != null ? event.getEventType() : "PAYMENT_EVENT";
        Object entityId = event.getPaymentId() != null ? event.getPaymentId() : event.getOrderId();
        Long customerId = event.getCustomerId();

        if ("REFUND_COMPLETED".equalsIgnoreCase(eventType) || "REFUND_SUCCESS".equalsIgnoreCase(eventType) || "REFUND_APPROVED".equalsIgnoreCase(eventType)) {
            String title = "Refund Processed for Order #" + event.getOrderId();
            String message = "A refund of $" + event.getAmount() + " has been credited for order #" + event.getOrderId() + " (Ref: " + (event.getTransactionRef() != null ? event.getTransactionRef() : "N/A") + ").";
            processNotification(customerId, "CUSTOMER", "Admin#" + DEFAULT_ADMIN_USER_ID, "ADMIN", entityId, eventType, title, message, "REFUND_SUCCESS");
            return;
        }

        if ("PAYMENT_SUCCESS".equalsIgnoreCase(eventType)) {
            String title = "Payment Successful for Order #" + event.getOrderId();
            String message = "Payment of $" + event.getAmount() + " via " + (event.getPaymentMethod() != null ? event.getPaymentMethod() : "N/A") + " was successful.";
            String actorId = customerId != null ? "Customer#" + customerId : "CUSTOMER";
            processNotification(customerId, "CUSTOMER", actorId, "CUSTOMER", entityId, eventType, title, message, "PAYMENT_SUCCESS");
            return;
        }

        String title = "Payment Update for Order #" + event.getOrderId();
        String message = "Payment status: " + event.getStatus() + ". " + (event.getMessage() != null ? event.getMessage() : "");
        processNotification(customerId, "CUSTOMER", "SYSTEM", "SYSTEM", entityId, eventType, title, message, "PAYMENT_STATUS");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DELIVERY_NOTIFICATIONS)
    public void handleDeliveryEvent(DeliveryEvent event) {
        if (event == null) {
            log.warn("Invalid delivery notification event.");
            return;
        }

        String eventType = event.getEventType() != null ? event.getEventType() : "DELIVERY_EVENT";
        Object entityId = event.getDeliveryId() != null ? event.getDeliveryId() : event.getOrderId();
        Long customerId = event.getCustomerId();
        Long agentId = event.getDeliveryAgentId();
        Long assigningAdminId = event.getAssignedByAdminId() != null ? event.getAssignedByAdminId() : DEFAULT_ADMIN_USER_ID;
        String statusUpper = event.getStatus() != null ? event.getStatus().toUpperCase() : "";

        // DELIVERY ACCEPTED -> Recipient: ASSIGNING ADMIN
        if ("ACCEPTED".equalsIgnoreCase(statusUpper) || "DELIVERY_ACCEPTED".equalsIgnoreCase(eventType)) {
            String title = "Delivery Assignment Accepted";
            String message = "Delivery partner has accepted the delivery assignment for order #" + event.getOrderId() + ".";
            String actorId = agentId != null ? "DeliveryAgent#" + agentId : "DELIVERY_PARTNER";
            processNotification(assigningAdminId, "ADMIN", actorId, "DELIVERY_PARTNER", entityId, eventType, title, message, "DELIVERY_ACCEPTED", statusUpper);
            return;
        }

        // DELIVERY REJECTED -> Recipient: ASSIGNING ADMIN
        if ("REJECTED".equalsIgnoreCase(statusUpper) || "DELIVERY_REJECTED".equalsIgnoreCase(eventType)) {
            String title = "Delivery Assignment Rejected";
            String message = "Delivery partner has rejected the delivery assignment for order #" + event.getOrderId() + ".";
            String actorId = agentId != null ? "DeliveryAgent#" + agentId : "DELIVERY_PARTNER";
            processNotification(assigningAdminId, "ADMIN", actorId, "DELIVERY_PARTNER", entityId, eventType, title, message, "DELIVERY_REJECTED", statusUpper);
            return;
        }

        // 3. ADMIN ASSIGNS DELIVERY PARTNER -> Recipient: DELIVERY_PARTNER ONLY
        if ("DELIVERY_ASSIGNED".equalsIgnoreCase(eventType) || "ASSIGNED".equalsIgnoreCase(statusUpper)) {
            String title = "New Delivery Assignment";
            String message = "New delivery assigned. Order #" + event.getOrderId();
            String actorId = "Admin#" + assigningAdminId;
            processNotification(agentId, "DELIVERY_PARTNER", actorId, "ADMIN", entityId, eventType, title, message, "DELIVERY_ASSIGNED", statusUpper);
            return;
        }

        // 4. DELIVERY PARTNER MARKS ORDER "OUT FOR DELIVERY" -> Recipient: BOTH
        if ("OUT_FOR_DELIVERY".equalsIgnoreCase(eventType) || "OUT_FOR_DELIVERY".equalsIgnoreCase(statusUpper)) {
            String title = "Order Out for Delivery #" + event.getOrderId();
            String message = "Your product is out for delivery and will arrive today.";
            String actorId = agentId != null ? "DeliveryAgent#" + agentId : "DELIVERY_PARTNER";
            processNotification(customerId, "CUSTOMER", actorId, "DELIVERY_PARTNER", entityId, eventType, title, message, "ORDER_OUT_FOR_DELIVERY", statusUpper);
            processNotification(agentId, "DELIVERY_PARTNER", actorId, "DELIVERY_PARTNER", entityId, eventType, title, message, "ORDER_OUT_FOR_DELIVERY", statusUpper);
            return;
        }

        // DELIVERY COMPLETION -> Recipient: BOTH
        if ("DELIVERED".equalsIgnoreCase(statusUpper) || "DELIVERY_COMPLETED".equalsIgnoreCase(eventType)) {
            String title = "Order Delivered! #" + event.getOrderId();
            String message = "Your package for order #" + event.getOrderId() + " has been delivered successfully. Thank you for shopping with EShoppingZone!";
            String actorId = agentId != null ? "DeliveryAgent#" + agentId : "DELIVERY_PARTNER";
            processNotification(customerId, "CUSTOMER", actorId, "DELIVERY_PARTNER", entityId, eventType, title, message, "DELIVERY_DELIVERED", statusUpper);
            processNotification(agentId, "DELIVERY_PARTNER", actorId, "DELIVERY_PARTNER", entityId, eventType, title, message, "DELIVERY_DELIVERED", statusUpper);
            return;
        }

        // FAILED DELIVERY -> Recipient: ALL THREE
        if ("FAILED".equalsIgnoreCase(statusUpper)) {
            String title = "Delivery Attempt Failed #" + event.getOrderId();
            String message = "Delivery attempt failed for order #" + event.getOrderId() + ". Reason: " + (event.getFailureReason() != null ? event.getFailureReason() : "Recipient unavailable");
            String actorId = agentId != null ? "DeliveryAgent#" + agentId : "DELIVERY_PARTNER";
            processNotification(customerId, "CUSTOMER", actorId, "DELIVERY_PARTNER", entityId, eventType, title, message, "DELIVERY_FAILED", statusUpper);
            processNotification(agentId, "DELIVERY_PARTNER", actorId, "DELIVERY_PARTNER", entityId, eventType, title, message, "DELIVERY_FAILED", statusUpper);
            processNotification(assigningAdminId, "ADMIN", actorId, "DELIVERY_PARTNER", entityId, eventType, title, message, "DELIVERY_FAILED", statusUpper);
            return;
        }

        // Default delivery status update for customer and agent
        String title = "Delivery Update for Order #" + event.getOrderId();
        String message = "Your order #" + event.getOrderId() + " delivery status is now " + event.getStatus() + ".";
        String actorId = agentId != null ? "DeliveryAgent#" + agentId : "DELIVERY_PARTNER";
        processNotification(customerId, "CUSTOMER", actorId, "DELIVERY_PARTNER", entityId, eventType, title, message, "DELIVERY_STATUS", statusUpper);
        processNotification(agentId, "DELIVERY_PARTNER", actorId, "DELIVERY_PARTNER", entityId, eventType, title, message, "DELIVERY_STATUS", statusUpper);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PRODUCT_NOTIFICATIONS)
    public void handleProductEvent(ProductEvent event) {
        if (event == null) {
            log.warn("Invalid product notification event.");
            return;
        }

        String eventType = event.getEventType() != null ? event.getEventType() : "PRODUCT_EVENT";
        Long productId = event.getProductId();
        String productName = event.getProductName() != null ? event.getProductName() : "Product #" + productId;
        Long merchantId = event.getMerchantId();
        String actorId = merchantId != null ? "Merchant#" + merchantId : "MERCHANT";

        // MERCHANT ADDS OR UPDATES PRODUCT -> Recipient: ADMIN
        if ("PRODUCT_CREATED".equalsIgnoreCase(eventType)) {
            String title = "New Product Added";
            String message = "New product '" + productName + "' added by Merchant.";
            processNotification(DEFAULT_ADMIN_USER_ID, "ADMIN", actorId, "MERCHANT", productId, eventType, title, message, "PRODUCT_CREATED");
            return;
        }

        if ("PRODUCT_UPDATED".equalsIgnoreCase(eventType)) {
            String title = "Product Updated";
            String message = "Product '" + productName + "' updated by Merchant.";
            processNotification(DEFAULT_ADMIN_USER_ID, "ADMIN", actorId, "MERCHANT", productId, eventType, title, message, "PRODUCT_UPDATED");
            return;
        }

        // ADMIN APPROVES PRODUCT -> Recipient: MERCHANT (PRODUCT OWNER)
        if ("PRODUCT_APPROVED".equalsIgnoreCase(eventType)) {
            String title = "Product Approved";
            String message = "Your product '" + productName + "' has been approved by the admin.";
            String adminActor = "Admin#" + DEFAULT_ADMIN_USER_ID;
            processNotification(merchantId, "MERCHANT", adminActor, "ADMIN", productId, eventType, title, message, "PRODUCT_APPROVED");
            return;
        }

        // ADMIN REJECTS PRODUCT -> Recipient: MERCHANT (PRODUCT OWNER)
        if ("PRODUCT_REJECTED".equalsIgnoreCase(eventType)) {
            String title = "Product Rejected";
            String message = "Your product '" + productName + "' has been rejected by the admin.";
            String adminActor = "Admin#" + DEFAULT_ADMIN_USER_ID;
            processNotification(merchantId, "MERCHANT", adminActor, "ADMIN", productId, eventType, title, message, "PRODUCT_REJECTED");
            return;
        }
    }
}
