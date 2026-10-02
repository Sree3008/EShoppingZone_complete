package com.eshoppingzone.settlement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_entries",
        indexes = {
                @Index(name = "idx_settlement_merchant_week", columnList = "merchant_id, settlement_week_start"),
                @Index(name = "idx_settlement_order", columnList = "order_id"),
                @Index(name = "idx_settlement_event", columnList = "event_id")
        })
public class SettlementEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "delivery_partner_id")
    private Long deliveryPartnerId;

    @Column(name = "event_id", length = 128, nullable = false, unique = true)
    private String eventId;

    @Column(name = "settlement_week_start", nullable = false)
    private LocalDate settlementWeekStart;

    @Column(name = "gross_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "refund_deduction", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundDeduction = BigDecimal.ZERO;

    @Column(name = "delivery_payout", nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryPayout = BigDecimal.ZERO;

    @Column(name = "return_delivery_payout", nullable = false, precision = 12, scale = 2)
    private BigDecimal returnDeliveryPayout = BigDecimal.ZERO;

    @Column(name = "cod_collection", nullable = false, precision = 12, scale = 2)
    private BigDecimal codCollection = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 30)
    private SettlementEntryType entryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = SettlementStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public SettlementEntry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getDeliveryPartnerId() { return deliveryPartnerId; }
    public void setDeliveryPartnerId(Long deliveryPartnerId) { this.deliveryPartnerId = deliveryPartnerId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public LocalDate getSettlementWeekStart() { return settlementWeekStart; }
    public void setSettlementWeekStart(LocalDate settlementWeekStart) { this.settlementWeekStart = settlementWeekStart; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public BigDecimal getRefundDeduction() { return refundDeduction; }
    public void setRefundDeduction(BigDecimal refundDeduction) { this.refundDeduction = refundDeduction; }
    public BigDecimal getDeliveryPayout() { return deliveryPayout; }
    public void setDeliveryPayout(BigDecimal deliveryPayout) { this.deliveryPayout = deliveryPayout; }
    public BigDecimal getReturnDeliveryPayout() { return returnDeliveryPayout; }
    public void setReturnDeliveryPayout(BigDecimal returnDeliveryPayout) { this.returnDeliveryPayout = returnDeliveryPayout; }
    public BigDecimal getCodCollection() { return codCollection; }
    public void setCodCollection(BigDecimal codCollection) { this.codCollection = codCollection; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public SettlementEntryType getEntryType() { return entryType; }
    public void setEntryType(SettlementEntryType entryType) { this.entryType = entryType; }
    public SettlementStatus getStatus() { return status; }
    public void setStatus(SettlementStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
