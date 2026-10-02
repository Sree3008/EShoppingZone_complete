package com.eshoppingzone.wishlist.service;

import com.eshoppingzone.wishlist.dto.AddToWishlistRequest;
import com.eshoppingzone.wishlist.dto.WishlistCheckResponse;
import com.eshoppingzone.wishlist.dto.WishlistDto;

public interface WishlistService {

    WishlistDto getMyWishlist(Long customerId);

    WishlistDto addToWishlist(Long customerId, AddToWishlistRequest request);

    WishlistDto removeFromWishlist(Long customerId, Long productId);

    WishlistCheckResponse isProductInWishlist(Long customerId, Long productId);

    void clearWishlist(Long customerId);

    WishlistDto getWishlistByCustomerId(Long customerId);
}
