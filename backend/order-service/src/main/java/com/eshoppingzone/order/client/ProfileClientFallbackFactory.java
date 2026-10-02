package com.eshoppingzone.order.client;

import com.eshoppingzone.order.dto.AddressDto;
import com.eshoppingzone.order.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ProfileClientFallbackFactory implements FallbackFactory<ProfileClient> {

    private static final Logger log = LoggerFactory.getLogger(ProfileClientFallbackFactory.class);

    @Override
    public ProfileClient create(Throwable cause) {
        return new ProfileClient() {
            @Override
            public ApiResponse<AddressDto> getAddressById(Long id) {
                String errMsg = cause != null ? cause.getMessage() : "Unknown cause";
                log.error("Fallback for getAddressById triggered due to: {}", errMsg);
                return ApiResponse.error("Failed to retrieve address: Service unavailable");
            }
        };
    }
}
