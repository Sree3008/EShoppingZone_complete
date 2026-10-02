package com.eshoppingzone.notification.consumer;

import com.eshoppingzone.notification.dto.DeliveryEvent;
import com.eshoppingzone.notification.dto.OrderEvent;
import com.eshoppingzone.notification.dto.PaymentEvent;
import com.eshoppingzone.notification.dto.ProductEvent;
import com.eshoppingzone.notification.dto.UserEvent;
import com.eshoppingzone.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationEventConsumerTest {

    private NotificationService notificationService;
    private NotificationEventConsumer consumer;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        consumer = new NotificationEventConsumer(notificationService);
    }

    @Test
    @DisplayName("Should handle UserRegisteredEvent and send notification to registered user")
    void handleUserRegistration_Success() {
        UserEvent event = new UserEvent();
        event.setEventType("USER_REGISTRATION");
        event.setUserId(101L);
        event.setUsername("john_doe");
        event.setEmail("john@example.com");
        event.setFullName("John Doe");
        event.setRole("CUSTOMER");

        consumer.handleUserRegistration(event);

        verify(notificationService).createNotification(
                eq(101L),
                eq("CUSTOMER"),
                eq("Welcome to EShoppingZone!"),
                eq("Hello John Doe, your account has been successfully created with role CUSTOMER."),
                eq("USER_REGISTRATION"),
                eq("USER_REGISTRATION-101-101")
        );
    }

    @Test
    @DisplayName("Test 1: Customer places order -> Sent ONLY to Merchant")
    void handleOrderEvent_CustomerPlacesOrder_SentOnlyToMerchant() {
        OrderEvent event = new OrderEvent();
        event.setEventType("ORDER_CONFIRMED");
        event.setOrderId(5001L);
        event.setCustomerId(101L);
        event.setMerchantId(202L);
        event.setTotalAmount(new BigDecimal("150.00"));
        event.setPaymentMethod("CREDIT_CARD");
        event.setStatus("CONFIRMED");

        consumer.handleOrderEvent(event);

        verify(notificationService, times(1)).createNotification(
                eq(202L),
                eq("MERCHANT"),
                eq("New Order Received #5001"),
                eq("You have received a new order #5001 for $150.00."),
                eq("MERCHANT_ORDER_RECEIVED"),
                eq("MERCHANT_ORDER_RECEIVED-5001-202")
        );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Test 2: Merchant packs order -> Sent ONLY to Admin")
    void handleOrderEvent_MerchantPacksOrder_SentOnlyToAdmin() {
        OrderEvent event = new OrderEvent();
        event.setEventType("ORDER_PROCESSING");
        event.setOrderId(5001L);
        event.setCustomerId(101L);
        event.setMerchantId(202L);
        event.setStatus("PROCESSING");

        consumer.handleOrderEvent(event);

        verify(notificationService, times(1)).createNotification(
                eq(1L),
                eq("ADMIN"),
                eq("Order Packed #5001"),
                eq("Order #5001 packed and ready."),
                eq("ORDER_PACKED"),
                eq("ORDER_PACKED-5001-1")
        );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Test 3: Admin assigns delivery partner -> Sent ONLY to Assigned Delivery Partner")
    void handleDeliveryEvent_AdminAssignsDelivery_SentOnlyToAssignedAgent() {
        DeliveryEvent event = new DeliveryEvent();
        event.setEventType("DELIVERY_ASSIGNED");
        event.setDeliveryId(9001L);
        event.setOrderId(5001L);
        event.setCustomerId(101L);
        event.setDeliveryAgentId(303L);
        event.setStatus("ASSIGNED");

        consumer.handleDeliveryEvent(event);

        verify(notificationService, times(1)).createNotification(
                eq(303L),
                eq("DELIVERY_PARTNER"),
                eq("New Delivery Assignment"),
                eq("New delivery assigned. Order #5001"),
                eq("DELIVERY_ASSIGNED"),
                eq("DELIVERY_ASSIGNED-9001-303-ASSIGNED")
        );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Test 4: Delivery Partner marks OUT_FOR_DELIVERY -> Sent to Customer and Agent")
    void handleDeliveryEvent_OutForDelivery_SentOnlyToCustomer() {
        DeliveryEvent event = new DeliveryEvent();
        event.setEventType("DELIVERY_STATUS_CHANGED");
        event.setDeliveryId(9001L);
        event.setOrderId(5001L);
        event.setCustomerId(101L);
        event.setDeliveryAgentId(303L);
        event.setStatus("OUT_FOR_DELIVERY");

        consumer.handleDeliveryEvent(event);

        verify(notificationService).createNotification(
                eq(101L),
                eq("CUSTOMER"),
                eq("Order Out for Delivery #5001"),
                eq("Your product is out for delivery and will arrive today."),
                eq("ORDER_OUT_FOR_DELIVERY"),
                eq("ORDER_OUT_FOR_DELIVERY-9001-101-OUT_FOR_DELIVERY")
        );
        verify(notificationService).createNotification(
                eq(303L),
                eq("DELIVERY_PARTNER"),
                eq("Order Out for Delivery #5001"),
                eq("Your product is out for delivery and will arrive today."),
                eq("ORDER_OUT_FOR_DELIVERY"),
                eq("ORDER_OUT_FOR_DELIVERY-9001-303-OUT_FOR_DELIVERY")
        );
    }

    @Test
    @DisplayName("Test 5: Delivery Completion -> Sent to Customer and Agent")
    void handleDeliveryEvent_Delivered_SentOnlyToCustomer() {
        DeliveryEvent event = new DeliveryEvent();
        event.setEventType("DELIVERY_STATUS_CHANGED");
        event.setDeliveryId(9001L);
        event.setOrderId(5001L);
        event.setCustomerId(101L);
        event.setDeliveryAgentId(303L);
        event.setStatus("DELIVERED");

        consumer.handleDeliveryEvent(event);

        verify(notificationService).createNotification(
                eq(101L),
                eq("CUSTOMER"),
                eq("Order Delivered! #5001"),
                eq("Your package for order #5001 has been delivered successfully. Thank you for shopping with EShoppingZone!"),
                eq("DELIVERY_DELIVERED"),
                eq("DELIVERY_DELIVERED-9001-101-DELIVERED")
        );
        verify(notificationService).createNotification(
                eq(303L),
                eq("DELIVERY_PARTNER"),
                eq("Order Delivered! #5001"),
                eq("Your package for order #5001 has been delivered successfully. Thank you for shopping with EShoppingZone!"),
                eq("DELIVERY_DELIVERED"),
                eq("DELIVERY_DELIVERED-9001-303-DELIVERED")
        );
    }

    @Test
    @DisplayName("Test 6: Customer requests refund -> Sent ONLY to Admin")
    void handleOrderEvent_RefundRequested_SentOnlyToAdmin() {
        OrderEvent event = new OrderEvent();
        event.setEventType("RETURN_REQUESTED");
        event.setOrderId(5001L);
        event.setCustomerId(101L);
        event.setMerchantId(202L);

        consumer.handleOrderEvent(event);

        verify(notificationService, times(1)).createNotification(
                eq(1L),
                eq("ADMIN"),
                eq("Refund Request Received #5001"),
                eq("Refund requested for Order #5001."),
                eq("REFUND_REQUESTED"),
                eq("REFUND_REQUESTED-5001-1")
        );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Test 7: Merchant adds product -> Sent ONLY to Admin")
    void handleProductEvent_ProductCreated_SentOnlyToAdmin() {
        ProductEvent event = new ProductEvent("PRODUCT_CREATED", 701L, "Wireless Mouse", 202L);

        consumer.handleProductEvent(event);

        verify(notificationService, times(1)).createNotification(
                eq(1L),
                eq("ADMIN"),
                eq("New Product Added"),
                eq("New product 'Wireless Mouse' added by Merchant."),
                eq("PRODUCT_CREATED"),
                eq("PRODUCT_CREATED-701-1")
        );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Test 8: Merchant updates product -> Sent ONLY to Admin")
    void handleProductEvent_ProductUpdated_SentOnlyToAdmin() {
        ProductEvent event = new ProductEvent("PRODUCT_UPDATED", 701L, "Wireless Mouse", 202L);

        consumer.handleProductEvent(event);

        verify(notificationService, times(1)).createNotification(
                eq(1L),
                eq("ADMIN"),
                eq("Product Updated"),
                eq("Product 'Wireless Mouse' updated by Merchant."),
                eq("PRODUCT_UPDATED"),
                eq("PRODUCT_UPDATED-701-1")
        );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Should safely skip processing when recipient user ID is null")
    void handleOrderEvent_NullMerchantIdOnOrderCreated_Skipped() {
        OrderEvent event = new OrderEvent();
        event.setEventType("ORDER_CONFIRMED");
        event.setOrderId(5001L);
        event.setCustomerId(101L);
        event.setMerchantId(null);

        consumer.handleOrderEvent(event);

        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("Should safely skip processing when event payload is null")
    void handleOrderEvent_NullEvent_Skipped() {
        consumer.handleOrderEvent(null);
        verifyNoInteractions(notificationService);
    }
}
