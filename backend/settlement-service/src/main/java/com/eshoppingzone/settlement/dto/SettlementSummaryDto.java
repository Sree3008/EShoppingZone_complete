package com.eshoppingzone.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SettlementSummaryDto {
    private Long merchantId;
    private LocalDate weekStart;
    private BigDecimal grossAmount = BigDecimal.ZERO;
    private BigDecimal refundDeduction = BigDecimal.ZERO;
    private BigDecimal deliveryPayout = BigDecimal.ZERO;
    private BigDecimal returnDeliveryPayout = BigDecimal.ZERO;
    private BigDecimal codCollection = BigDecimal.ZERO;
    private BigDecimal netAmount = BigDecimal.ZERO;
    private BigDecimal outstandingAmount = BigDecimal.ZERO;
    private int ordersCount;

    public SettlementSummaryDto() {}

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }
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
    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public void setOutstandingAmount(BigDecimal outstandingAmount) { this.outstandingAmount = outstandingAmount; }
    public int getOrdersCount() { return ordersCount; }
    public void setOrdersCount(int ordersCount) { this.ordersCount = ordersCount; }
}
