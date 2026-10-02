package com.eshoppingzone.order.client;

import com.eshoppingzone.order.dto.ApiResponse;
import com.eshoppingzone.order.dto.ProductSnapshotDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    private static final Logger log = LoggerFactory.getLogger(ProductClientFallbackFactory.class);

    @Override
    public ProductClient create(Throwable cause) {
        return new ProductClient() {
            @Override
            public ApiResponse<ProductSnapshotDto> getProductById(Long id) {
                String errMsg = cause != null ? cause.getMessage() : "Unknown cause";
                log.error("Fallback for getProductById triggered due to: {}", errMsg);
                return ApiResponse.error("Failed to retrieve product: Service unavailable");
            }
        };
    }
}
