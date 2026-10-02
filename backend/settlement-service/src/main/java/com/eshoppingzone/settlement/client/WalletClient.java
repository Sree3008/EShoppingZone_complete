package com.eshoppingzone.settlement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@FeignClient(name = "wallet-service", path = "/api/v1/wallet")
public interface WalletClient {
    @PostMapping("/credit")
    Object credit(@RequestHeader("Authorization") String authorization,
                  @RequestHeader("Idempotency-Key") String idempotencyKey,
                  @RequestBody WalletCreditRequest request);

    class WalletCreditRequest {
        private Long userId;
        private BigDecimal amount;
        private String reference;
        private String description;
        public WalletCreditRequest() {}
        public WalletCreditRequest(Long userId, BigDecimal amount, String reference, String description) {
            this.userId = userId; this.amount = amount; this.reference = reference; this.description = description;
        }
        public Long getUserId() { return userId; } public void setUserId(Long v) { userId = v; }
        public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal v) { amount = v; }
        public String getReference() { return reference; } public void setReference(String v) { reference = v; }
        public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    }
}
