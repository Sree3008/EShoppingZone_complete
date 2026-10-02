package com.eshoppingzone.settlement.controller;

import com.eshoppingzone.settlement.dto.*;
import com.eshoppingzone.settlement.entity.CodRecord;
import com.eshoppingzone.settlement.entity.SettlementEntry;
import com.eshoppingzone.settlement.security.UserPrincipal;
import com.eshoppingzone.settlement.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/settlements")
public class SettlementController {
    private final SettlementService service;
    public SettlementController(SettlementService service) { this.service = service; }

    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN','INTERNAL')")
    public ResponseEntity<ApiResponse<Void>> event(@Valid @RequestBody SettlementEventRequest r) {
        service.processSettlementEvent(r.getEventType(), r.getMerchantId(), r.getOrderId(), r.getAmount(), r.getEventId());
        return ResponseEntity.accepted().body(ApiResponse.success("Settlement event accepted", null));
    }

    @GetMapping("/merchant/{merchantId}")
    @PreAuthorize("hasAnyRole('ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<SettlementSummaryDto>> merchant(@PathVariable Long merchantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MERCHANT"))
                && !merchantId.equals(((UserPrincipal) auth.getPrincipal()).getUserId()))
            return ResponseEntity.status(403).body(ApiResponse.error("Merchants may only view their own settlement"));
        return ResponseEntity.ok(ApiResponse.success("Settlement retrieved", service.getMerchantSettlementSummary(merchantId, weekStart)));
    }

    @PostMapping("/weekly/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SettlementSummaryDto>>> runWeekly() {
        return ResponseEntity.ok(ApiResponse.success("Previous week settlement processed", service.settlePreviousWeek()));
    }

    @PostMapping("/cod/expected")
    @PreAuthorize("hasAnyRole('ADMIN','INTERNAL')")
    public ResponseEntity<ApiResponse<CodRecord>> codExpected(@Valid @RequestBody SettlementEventRequest r) {
        return ResponseEntity.ok(ApiResponse.success("COD expected recorded",
                service.expectCod(r.getMerchantId(), r.getOrderId(), r.getDeliveryPartnerId(), r.getExpectedAmount(), r.getEventId())));
    }

    @PostMapping("/cod/collect")
    @PreAuthorize("hasAnyRole('ADMIN','DELIVERY_PARTNER','INTERNAL')")
    public ResponseEntity<ApiResponse<CodRecord>> codCollect(@Valid @RequestBody SettlementEventRequest r) {
        return ResponseEntity.ok(ApiResponse.success("COD collection recorded", service.collectCod(r.getOrderId(), r.getAmount(), r.getEventId())));
    }

    @PostMapping("/cod/remit")
    @PreAuthorize("hasAnyRole('ADMIN','DELIVERY_PARTNER','INTERNAL')")
    public ResponseEntity<ApiResponse<CodRecord>> codRemit(@Valid @RequestBody SettlementEventRequest r) {
        return ResponseEntity.ok(ApiResponse.success("COD remittance recorded", service.remitCod(r.getOrderId(), r.getAmount(), r.getEventId())));
    }

    @PostMapping("/cod/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CodRecord>> codConfirm(@Valid @RequestBody SettlementEventRequest r) {
        return ResponseEntity.ok(ApiResponse.success("COD receipt confirmed", service.confirmCodReceipt(r.getOrderId(), r.getAmount(), r.getEventId())));
    }

    @PostMapping("/delivery-payout")
    @PreAuthorize("hasAnyRole('ADMIN','INTERNAL')")
    public ResponseEntity<ApiResponse<SettlementEntry>> deliveryPayout(@Valid @RequestBody SettlementEventRequest r) {
        return ResponseEntity.ok(ApiResponse.success("Delivery payout recorded",
                service.recordDeliveryPayout(r.getMerchantId(), r.getOrderId(), r.getDeliveryPartnerId(), r.getEventId())));
    }

    @PostMapping("/return-payout")
    @PreAuthorize("hasAnyRole('ADMIN','INTERNAL')")
    public ResponseEntity<ApiResponse<SettlementEntry>> returnPayout(@Valid @RequestBody SettlementEventRequest r) {
        return ResponseEntity.ok(ApiResponse.success("Return payout recorded",
                service.recordReturnDeliveryPayout(r.getMerchantId(), r.getOrderId(), r.getDeliveryPartnerId(), r.getEventId())));
    }
}
