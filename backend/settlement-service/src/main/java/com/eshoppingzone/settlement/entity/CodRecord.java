package com.eshoppingzone.settlement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cod_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cod_order", columnNames = {"order_id"}),
        @UniqueConstraint(name = "uk_cod_event", columnNames = {"last_event_id"})
})
public class CodRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "merchant_id", nullable = false) private Long merchantId;
    @Column(name = "order_id", nullable = false) private Long orderId;
    @Column(name = "delivery_partner_id") private Long deliveryPartnerId;
    @Column(name = "expected_amount", nullable = false, precision = 12, scale = 2) private BigDecimal expectedAmount;
    @Column(name = "collected_amount", nullable = false, precision = 12, scale = 2) private BigDecimal collectedAmount = BigDecimal.ZERO;
    @Column(name = "remitted_amount", nullable = false, precision = 12, scale = 2) private BigDecimal remittedAmount = BigDecimal.ZERO;
    @Column(name = "received_amount", nullable = false, precision = 12, scale = 2) private BigDecimal receivedAmount = BigDecimal.ZERO;
    @Column(name = "outstanding_amount", nullable = false, precision = 12, scale = 2) private BigDecimal outstandingAmount = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private CodStatus status = CodStatus.EXPECTED;
    @Column(name = "last_event_id", nullable = false, unique = true) private String lastEventId;
    @Column(nullable = false) private LocalDateTime updatedAt;
    @PrePersist @PreUpdate void touch() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Long getMerchantId() { return merchantId; } public void setMerchantId(Long v) { merchantId = v; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long v) { orderId = v; }
    public Long getDeliveryPartnerId() { return deliveryPartnerId; } public void setDeliveryPartnerId(Long v) { deliveryPartnerId = v; }
    public BigDecimal getExpectedAmount() { return expectedAmount; } public void setExpectedAmount(BigDecimal v) { expectedAmount = v; }
    public BigDecimal getCollectedAmount() { return collectedAmount; } public void setCollectedAmount(BigDecimal v) { collectedAmount = v; }
    public BigDecimal getRemittedAmount() { return remittedAmount; } public void setRemittedAmount(BigDecimal v) { remittedAmount = v; }
    public BigDecimal getReceivedAmount() { return receivedAmount; } public void setReceivedAmount(BigDecimal v) { receivedAmount = v; }
    public BigDecimal getOutstandingAmount() { return outstandingAmount; } public void setOutstandingAmount(BigDecimal v) { outstandingAmount = v; }
    public CodStatus getStatus() { return status; } public void setStatus(CodStatus v) { status = v; }
    public String getLastEventId() { return lastEventId; } public void setLastEventId(String v) { lastEventId = v; }
}
