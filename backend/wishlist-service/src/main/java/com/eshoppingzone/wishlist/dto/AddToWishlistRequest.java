package com.eshoppingzone.wishlist.dto;

import jakarta.validation.constraints.NotNull;

public class AddToWishlistRequest {

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    public AddToWishlistRequest() {
    }

    public AddToWishlistRequest(Long productId) {
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
