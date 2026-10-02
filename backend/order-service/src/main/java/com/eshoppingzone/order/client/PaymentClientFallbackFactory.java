package com.eshoppingzone.order.client;

import com.eshoppingzone.order.dto.ApiResponse;
import com.eshoppingzone.order.dto.PaymentResponseDto;
import com.eshoppingzone.order.dto.ProcessPaymentRequest;
import com.eshoppingzone.order.returns.dto.RefundRequestDto;
import com.eshoppingzone.order.returns.dto.RefundResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentClientFallbackFactory implements FallbackFactory<PaymentClient> {

    private static final Logger log = LoggerFactory.getLogger(PaymentClientFallbackFactory.class);

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
    public PaymentClient create(Throwable cause) {
        return new PaymentClient() {
            @Override
            public ApiResponse<PaymentResponseDto> processPayment(ProcessPaymentRequest request) {
                String errMsg = extractErrorMessage(cause, "Failed to process payment: Service unavailable");
                log.error("Fallback for processPayment triggered due to: {}", errMsg);
                return ApiResponse.error(errMsg);
            }

            @Override
            public ApiResponse<PaymentResponseDto> getPaymentByOrderId(Long orderId) {
                String errMsg = extractErrorMessage(cause, "Failed to retrieve payment: Service unavailable");
                log.error("Fallback for getPaymentByOrderId triggered due to: {}", errMsg);
                return ApiResponse.error(errMsg);
            }

            @Override
            public ApiResponse<RefundResponseDto> requestRefundInternal(RefundRequestDto request) {
                String errMsg = extractErrorMessage(cause, "Failed to request refund: Service unavailable");
                log.error("Fallback for requestRefundInternal triggered due to: {}", errMsg);
                return ApiResponse.error(errMsg);
            }
        };
    }
}

