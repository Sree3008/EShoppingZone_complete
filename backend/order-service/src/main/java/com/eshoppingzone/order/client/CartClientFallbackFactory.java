package com.eshoppingzone.order.client;

import com.eshoppingzone.order.dto.ApiResponse;
import com.eshoppingzone.order.dto.CartDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class CartClientFallbackFactory implements FallbackFactory<CartClient> {

    private static final Logger log = LoggerFactory.getLogger(CartClientFallbackFactory.class);

    @Override
    public CartClient create(Throwable cause) {
        return new CartClient() {
            @Override
            public ApiResponse<CartDto> getCartByCustomerId(Long customerId) {
                String errMsg = cause != null ? cause.getMessage() : "Unknown cause";
                log.error("Fallback for getCartByCustomerId triggered due to: {}", errMsg);
                return ApiResponse.error("Failed to retrieve cart: Service unavailable");
            }

            @Override
            public ApiResponse<Void> clearCustomerCart(Long customerId) {
                String errMsg = cause != null ? cause.getMessage() : "Unknown cause";
                log.error("Fallback for clearCustomerCart triggered due to: {}", errMsg);
                return ApiResponse.error("Failed to clear cart: Service unavailable");
            }
        };
    }
}
