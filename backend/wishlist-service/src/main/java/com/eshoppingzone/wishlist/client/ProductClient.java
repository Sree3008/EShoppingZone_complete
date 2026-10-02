package com.eshoppingzone.wishlist.client;

import com.eshoppingzone.wishlist.dto.ApiResponse;
import com.eshoppingzone.wishlist.dto.ProductSnapshotDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ApiResponse<ProductSnapshotDto> getProductById(@PathVariable("id") Long id);
}
