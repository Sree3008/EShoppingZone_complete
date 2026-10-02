package com.eshoppingzone.order.client;

import com.eshoppingzone.order.dto.ApiResponse;
import com.eshoppingzone.order.dto.StockReservationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {
    
    private static final Logger log = LoggerFactory.getLogger(InventoryClientFallbackFactory.class);

    private String extractErrorMessage(Throwable cause, String defaultMsg) {
        if (cause == null) return defaultMsg;
        if (cause instanceof feign.FeignException feignException) {
            try {
                String content = feignException.contentUTF8();
                if (content != null && !content.isBlank()) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(content);
                    if (node.has("message") && !node.get("message").isNull()) {
                        return node.get("message").asText();
                    }
                }
            } catch (Exception ignored) {
            }
            if (feignException.getMessage() != null && !feignException.getMessage().isBlank()) {
                return feignException.getMessage();
            }
        }
        return cause.getMessage() != null ? cause.getMessage() : defaultMsg;
    }

    @Override
    public InventoryClient create(Throwable cause) {
        return new InventoryClient() {
            @Override
            public ApiResponse<Void> reserveStock(StockReservationRequest request) {
                String errMsg = extractErrorMessage(cause, "Inventory reservation failed for order items");
                log.error("Fallback for reserveStock triggered due to: {}", errMsg);
                return ApiResponse.error(errMsg);
            }

            @Override
            public ApiResponse<Void> releaseStock(StockReservationRequest request) {
                String errMsg = extractErrorMessage(cause, "Failed to release inventory: Service unavailable or error occurred");
                log.error("Fallback for releaseStock triggered due to: {}", errMsg);
                return ApiResponse.error(errMsg);
            }

            @Override
            public ApiResponse<Void> confirmStock(StockReservationRequest request) {
                String errMsg = extractErrorMessage(cause, "Failed to confirm inventory: Service unavailable or error occurred");
                log.error("Fallback for confirmStock triggered due to: {}", errMsg);
                return ApiResponse.error(errMsg);
            }

            @Override
            public ApiResponse<Void> restock(StockReservationRequest request) {
                String errMsg = extractErrorMessage(cause, "Failed to restock inventory: Service unavailable or error occurred");
                log.error("Fallback for restock triggered due to: {}", errMsg);
                return ApiResponse.error(errMsg);
            }
        };
    }
}

