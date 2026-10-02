package com.eshoppingzone.wishlist.controller;

import com.eshoppingzone.wishlist.dto.AddToWishlistRequest;
import com.eshoppingzone.wishlist.dto.ApiResponse;
import com.eshoppingzone.wishlist.dto.WishlistCheckResponse;
import com.eshoppingzone.wishlist.dto.WishlistDto;
import com.eshoppingzone.wishlist.security.UserPrincipal;
import com.eshoppingzone.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
@Tag(name = "Wishlist Management", description = "APIs for managing customer wishlists")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    private Long getCustomerId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUserId();
        }
        throw new com.eshoppingzone.wishlist.exception.BadRequestException("User not authenticated or customer ID not found");
    }

    @GetMapping
    @Operation(summary = "Get My Wishlist", description = "Retrieve all items in the customer's wishlist")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<WishlistDto>> getMyWishlist(Authentication authentication) {
        Long customerId = getCustomerId(authentication);
        WishlistDto wishlist = wishlistService.getMyWishlist(customerId);
        return ResponseEntity.ok(ApiResponse.success("Wishlist retrieved successfully", wishlist));
    }

    @PostMapping("/items")
    @Operation(summary = "Add Item to Wishlist", description = "Add a product to the authenticated customer's wishlist")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<WishlistDto>> addToWishlist(
            Authentication authentication,
            @Valid @RequestBody AddToWishlistRequest request) {
        Long customerId = getCustomerId(authentication);
        WishlistDto wishlist = wishlistService.addToWishlist(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to wishlist", wishlist));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove Item from Wishlist", description = "Remove a product from the customer's wishlist")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<WishlistDto>> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        Long customerId = getCustomerId(authentication);
        WishlistDto wishlist = wishlistService.removeFromWishlist(customerId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from wishlist", wishlist));
    }

    @GetMapping("/check/{productId}")
    @Operation(summary = "Check Product in Wishlist", description = "Check if a specific product is saved in the customer's wishlist")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<WishlistCheckResponse>> checkProductInWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        Long customerId = getCustomerId(authentication);
        WishlistCheckResponse response = wishlistService.isProductInWishlist(customerId, productId);
        return ResponseEntity.ok(ApiResponse.success("Wishlist status checked", response));
    }

    @DeleteMapping
    @Operation(summary = "Clear Wishlist", description = "Remove all products from the customer's wishlist")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> clearWishlist(Authentication authentication) {
        Long customerId = getCustomerId(authentication);
        wishlistService.clearWishlist(customerId);
        return ResponseEntity.ok(ApiResponse.success("Wishlist cleared successfully", null));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get Customer Wishlist (Internal/Admin)", description = "Internal endpoint for other microservices or admins")
    @PreAuthorize("hasAnyRole('INTERNAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<WishlistDto>> getWishlistByCustomerId(@PathVariable Long customerId) {
        WishlistDto wishlist = wishlistService.getWishlistByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success("Wishlist retrieved successfully", wishlist));
    }
}
