package com.eshoppingzone.settlement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SettlementEventRequest {
    @NotBlank private String eventType;
    @NotBlank private String eventId;
    @NotNull private Long merchantId;
    @NotNull private Long orderId;
    private Long deliveryPartnerId;
    private BigDecimal amount;
    private BigDecimal expectedAmount;
    public String getEventType() { return eventType; } public void setEventType(String v) { eventType = v; }
    public String getEventId() { return eventId; } public void setEventId(String v) { eventId = v; }
    public Long getMerchantId() { return merchantId; } public void setMerchantId(Long v) { merchantId = v; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long v) { orderId = v; }
    public Long getDeliveryPartnerId() { return deliveryPartnerId; } public void setDeliveryPartnerId(Long v) { deliveryPartnerId = v; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal v) { amount = v; }
    public BigDecimal getExpectedAmount() { return expectedAmount; } public void setExpectedAmount(BigDecimal v) { expectedAmount = v; }
}
