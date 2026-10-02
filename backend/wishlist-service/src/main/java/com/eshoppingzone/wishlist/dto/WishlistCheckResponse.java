package com.eshoppingzone.wishlist.dto;

public class WishlistCheckResponse {

    private Long productId;
    private boolean inWishlist;

    public WishlistCheckResponse() {
    }

    public WishlistCheckResponse(Long productId, boolean inWishlist) {
        this.productId = productId;
        this.inWishlist = inWishlist;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public boolean isInWishlist() {
        return inWishlist;
    }

    public void setInWishlist(boolean inWishlist) {
        this.inWishlist = inWishlist;
    }
}
