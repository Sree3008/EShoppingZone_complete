package com.eshoppingzone.settlement.service;

import com.eshoppingzone.settlement.dto.SettlementRequest;
import com.eshoppingzone.settlement.entity.*;
import com.eshoppingzone.settlement.repository.CodRepository;
import com.eshoppingzone.settlement.repository.SettlementRepository;
import com.eshoppingzone.settlement.client.WalletClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SettlementServiceImplTest {
    @Mock SettlementRepository entries;
    @Mock CodRepository cods;
    @Mock WalletClient walletClient;
    SettlementServiceImpl service;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SettlementServiceImpl(entries, cods);
        service.setWalletClient(walletClient);
        when(entries.findByEventIdForUpdate(anyString())).thenReturn(Optional.empty());
        when(entries.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test void onlineEarningIsAttributedAndPending() {
        SettlementRequest r = request("online-1", new BigDecimal("1000"));
        SettlementEntry result = service.recordOrderSettlement(r);
        assertEquals(1L, result.getMerchantId());
        assertEquals(new BigDecimal("1000"), result.getGrossAmount());
        assertEquals(SettlementStatus.PROCESSED, result.getStatus());
        assertNotEquals(SettlementStatus.SETTLED, result.getStatus());
    }

    @Test void duplicateDeliveryEventReturnsOriginalAndDoesNotSaveTwice() {
        SettlementEntry original = new SettlementEntry();
        original.setEventId("delivery-1");
        when(entries.findByEventIdForUpdate("delivery-1")).thenReturn(Optional.of(original));
        SettlementEntry result = service.recordDeliveryPayout(1L, 10L, 99L, "delivery-1");
        assertSame(original, result);
        verify(entries, never()).save(any());
    }

    @Test void partialCodReceiptKeepsOutstandingAndAttributesMerchant() {
        CodRecord record = new CodRecord();
        record.setMerchantId(7L); record.setOrderId(10L); record.setExpectedAmount(new BigDecimal("1000"));
        when(cods.findByOrderIdForUpdate(10L)).thenReturn(Optional.of(record));
        when(cods.save(any())).thenAnswer(i -> i.getArgument(0));
        CodRecord result = service.confirmCodReceipt(10L, new BigDecimal("900"), "cod-received-1");
        assertEquals(7L, result.getMerchantId());
        assertEquals(new BigDecimal("100"), result.getOutstandingAmount());
        assertEquals(CodStatus.RECEIVED, result.getStatus());
    }

    @Test void weeklySettlementUsesPreviousMondayOnly() {
        LocalDate previous = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
        SettlementEntry candidate = new SettlementEntry();
        candidate.setMerchantId(1L); candidate.setOrderId(10L); candidate.setSettlementWeekStart(previous);
        candidate.setGrossAmount(new BigDecimal("100")); candidate.setRefundDeduction(BigDecimal.ZERO);
        candidate.setDeliveryPayout(BigDecimal.ZERO); candidate.setReturnDeliveryPayout(BigDecimal.ZERO);
        candidate.setCodCollection(BigDecimal.ZERO); candidate.setStatus(SettlementStatus.PROCESSED);
        when(entries.findByWeekAndStatus(previous, SettlementStatus.PROCESSED)).thenReturn(List.of(candidate));
        when(entries.findByMerchantAndWeekAndStatus(1L, previous, SettlementStatus.PROCESSED)).thenReturn(List.of(candidate));
        when(entries.existsByMerchantIdAndSettlementWeekStartAndEntryType(1L, previous, SettlementEntryType.WEEKLY_SETTLEMENT)).thenReturn(false);
        assertEquals(1, service.settlePreviousWeek().size());
        assertEquals(SettlementStatus.SETTLED, candidate.getStatus());
        verify(entries).saveAll(anyList());
        verify(walletClient).credit(anyString(), eq("SETTLEMENT-1-" + previous),
                argThat(r -> r.getAmount().compareTo(new BigDecimal("100")) == 0));
    }

    @Test void duplicateWeeklySettlementUsesSameWalletIdempotencyReference() {
        LocalDate previous = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
        SettlementEntry candidate = candidate(1L, previous, "order-1", new BigDecimal("100"));
        when(entries.findByWeekAndStatus(previous, SettlementStatus.PROCESSED)).thenReturn(List.of(candidate));
        when(entries.findByWeekAndEntryType(previous, SettlementEntryType.WEEKLY_SETTLEMENT))
                .thenReturn(List.of());
        when(entries.findByMerchantAndWeekAndEntryType(1L, previous, SettlementEntryType.WEEKLY_SETTLEMENT))
                .thenReturn(List.of());
        when(entries.findByMerchantAndWeekAndStatus(1L, previous, SettlementStatus.PROCESSED))
                .thenReturn(List.of(candidate));
        service.settlePreviousWeek();
        SettlementEntry weekly = new SettlementEntry();
        weekly.setMerchantId(1L);
        weekly.setOrderId(-1L);
        weekly.setEventId("WEEKLY-1-" + previous);
        weekly.setSettlementWeekStart(previous);
        weekly.setNetAmount(new BigDecimal("100"));
        weekly.setStatus(SettlementStatus.SETTLED);
        when(entries.findByWeekAndStatus(previous, SettlementStatus.PROCESSED)).thenReturn(List.of());
        when(entries.findByWeekAndEntryType(previous, SettlementEntryType.WEEKLY_SETTLEMENT))
                .thenReturn(List.of(weekly));
        when(entries.findByMerchantAndWeekAndEntryType(1L, previous, SettlementEntryType.WEEKLY_SETTLEMENT))
                .thenReturn(List.of(weekly));
        service.settlePreviousWeek();
        verify(walletClient, times(2)).credit(anyString(), eq("SETTLEMENT-1-" + previous), any());
    }

    @Test void deductionDeficitPersistsAndCarriesForward() {
        LocalDate previous = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
        SettlementEntry earning = candidate(2L, previous, "order-2", new BigDecimal("10"));
        SettlementEntry deduction = candidate(2L, previous, "refund-2", BigDecimal.ZERO);
        deduction.setRefundDeduction(new BigDecimal("50"));
        when(entries.findByWeekAndStatus(previous, SettlementStatus.PROCESSED)).thenReturn(List.of(earning, deduction));
        when(entries.findByWeekAndEntryType(previous, SettlementEntryType.WEEKLY_SETTLEMENT)).thenReturn(List.of());
        when(entries.findByMerchantAndWeekAndStatus(2L, previous, SettlementStatus.PROCESSED))
                .thenReturn(List.of(earning, deduction));
        when(entries.findByMerchantAndEntryTypeAndStatus(2L, SettlementEntryType.MERCHANT_OUTSTANDING,
                SettlementStatus.OUTSTANDING)).thenReturn(List.of());
        service.settlePreviousWeek();
        verify(entries, atLeastOnce()).save(argThat(e -> e.getEntryType() == SettlementEntryType.MERCHANT_OUTSTANDING
                && e.getNetAmount().compareTo(new BigDecimal("40")) == 0));
    }

    private SettlementEntry candidate(Long merchantId, LocalDate week, String eventId, BigDecimal gross) {
        SettlementEntry entry = new SettlementEntry();
        entry.setMerchantId(merchantId);
        entry.setOrderId(10L);
        entry.setEventId(eventId);
        entry.setSettlementWeekStart(week);
        entry.setGrossAmount(gross);
        entry.setRefundDeduction(BigDecimal.ZERO);
        entry.setDeliveryPayout(BigDecimal.ZERO);
        entry.setReturnDeliveryPayout(BigDecimal.ZERO);
        entry.setCodCollection(BigDecimal.ZERO);
        entry.setNetAmount(gross);
        entry.setStatus(SettlementStatus.PROCESSED);
        return entry;
    }

    private SettlementRequest request(String event, BigDecimal amount) {
        SettlementRequest r = new SettlementRequest();
        r.setMerchantId(1L); r.setOrderId(10L); r.setEventId(event); r.setGrossAmount(amount);
        return r;
    }
}
