package com.eshoppingzone.inventory.dto;

import java.time.LocalDateTime;

public class ProductEvent {

    private String eventType;
    private Long productId;
    private String productName;
    private Long merchantId;
    private LocalDateTime timestamp;

    public ProductEvent() {
    }

    public ProductEvent(String eventType, Long productId, String productName, Long merchantId) {
        this.eventType = eventType;
        this.productId = productId;
        this.productName = productName;
        this.merchantId = merchantId;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
