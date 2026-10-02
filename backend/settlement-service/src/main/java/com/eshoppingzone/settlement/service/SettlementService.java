package com.eshoppingzone.settlement.service;

import com.eshoppingzone.settlement.dto.SettlementRequest;
import com.eshoppingzone.settlement.dto.SettlementSummaryDto;
import com.eshoppingzone.settlement.entity.SettlementEntry;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import com.eshoppingzone.settlement.entity.CodRecord;

public interface SettlementService {
    SettlementEntry recordOrderSettlement(SettlementRequest request);
    SettlementEntry recordCodCollection(Long merchantId, Long orderId, String eventId, java.math.BigDecimal amount);
    SettlementEntry recordDeliveryPayout(Long merchantId, Long orderId, String eventId, java.math.BigDecimal amount);
    SettlementEntry recordReturnDeliveryPayout(Long merchantId, Long orderId, String eventId, java.math.BigDecimal amount);
    SettlementEntry recordRefundDeduction(Long merchantId, Long orderId, String eventId, java.math.BigDecimal amount);
    SettlementSummaryDto getMerchantSettlementSummary(Long merchantId, LocalDate weekStart);
    List<SettlementSummaryDto> runWeeklySettlement();
    void processSettlementEvent(String eventType, Long merchantId, Long orderId, java.math.BigDecimal amount, String eventId);
    SettlementEntry recordDeliveryPayout(Long merchantId, Long orderId, Long partnerId, String eventId);
    SettlementEntry recordReturnDeliveryPayout(Long merchantId, Long orderId, Long partnerId, String eventId);
    CodRecord expectCod(Long merchantId, Long orderId, Long partnerId, BigDecimal amount, String eventId);
    CodRecord collectCod(Long orderId, BigDecimal amount, String eventId);
    CodRecord remitCod(Long orderId, BigDecimal amount, String eventId);
    CodRecord confirmCodReceipt(Long orderId, BigDecimal amount, String eventId);
    List<SettlementSummaryDto> settlePreviousWeek();
}
