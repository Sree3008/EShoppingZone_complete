package com.eshoppingzone.wishlist.service;

import com.eshoppingzone.wishlist.client.ProductClient;
import com.eshoppingzone.wishlist.dto.*;
import com.eshoppingzone.wishlist.entity.Wishlist;
import com.eshoppingzone.wishlist.entity.WishlistItem;
import com.eshoppingzone.wishlist.exception.ResourceNotFoundException;
import com.eshoppingzone.wishlist.repository.WishlistItemRepository;
import com.eshoppingzone.wishlist.repository.WishlistRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class WishlistServiceImpl implements WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistServiceImpl.class);

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductClient productClient;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               WishlistItemRepository wishlistItemRepository,
                               ProductClient productClient) {
        this.wishlistRepository = wishlistRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.productClient = productClient;
    }

    private Wishlist getOrCreateWishlist(Long customerId) {
        return wishlistRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setCustomerId(customerId);
                    return wishlistRepository.save(wishlist);
                });
    }

    @Override
    @Transactional
    public WishlistDto getMyWishlist(Long customerId) {
        Wishlist wishlist = getOrCreateWishlist(customerId);
        return WishlistDto.fromEntity(wishlist);
    }

    @Override
    @Transactional
    public WishlistDto addToWishlist(Long customerId, AddToWishlistRequest request) {
        Wishlist wishlist = getOrCreateWishlist(customerId);

        // Check if already in wishlist
        boolean alreadyExists = wishlist.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(request.getProductId()));
        if (alreadyExists) {
            log.info("Product {} is already in wishlist for customer {}", request.getProductId(), customerId);
            return WishlistDto.fromEntity(wishlist);
        }

        // Fetch product snapshot from Product Service
        String productName = "Product #" + request.getProductId();
        BigDecimal unitPrice = BigDecimal.ZERO;
        String category = null;
        String imageUrl = null;

        if (productClient != null) {
            try {
                ApiResponse<ProductSnapshotDto> productResponse = productClient.getProductById(request.getProductId());
                if (productResponse != null && productResponse.isSuccess() && productResponse.getData() != null) {
                    ProductSnapshotDto product = productResponse.getData();
                    productName = product.getName() != null ? product.getName() : productName;
                    unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
                    category = product.getCategory();
                    imageUrl = product.getImageUrl();
                }
            } catch (FeignException.NotFound e) {
                throw new ResourceNotFoundException("Product not found with id: " + request.getProductId());
            } catch (Exception e) {
                log.warn("Could not retrieve product info from ProductClient for productId {}: {}", request.getProductId(), e.getMessage());
            }
        }

        WishlistItem item = new WishlistItem();
        item.setWishlist(wishlist);
        item.setProductId(request.getProductId());
        item.setProductName(productName);
        item.setUnitPrice(unitPrice);
        item.setCategory(category);
        item.setImageUrl(imageUrl);

        wishlist.getItems().add(item);
        Wishlist saved = wishlistRepository.save(wishlist);
        log.info("Added product {} to wishlist for customer {}", request.getProductId(), customerId);
        return WishlistDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public WishlistDto removeFromWishlist(Long customerId, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(customerId);

        boolean removed = wishlist.getItems().removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new ResourceNotFoundException("Product with id " + productId + " is not in the wishlist");
        }

        Wishlist saved = wishlistRepository.save(wishlist);
        log.info("Removed product {} from wishlist for customer {}", productId, customerId);
        return WishlistDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistCheckResponse isProductInWishlist(Long customerId, Long productId) {
        Optional<Wishlist> opt = wishlistRepository.findByCustomerId(customerId);
        boolean inWishlist = opt.map(w -> w.getItems().stream()
                .anyMatch(i -> i.getProductId().equals(productId)))
                .orElse(false);
        return new WishlistCheckResponse(productId, inWishlist);
    }

    @Override
    @Transactional
    public void clearWishlist(Long customerId) {
        Wishlist wishlist = getOrCreateWishlist(customerId);
        wishlist.getItems().clear();
        wishlistRepository.save(wishlist);
        log.info("Cleared wishlist for customer {}", customerId);
    }

    @Override
    @Transactional
    public WishlistDto getWishlistByCustomerId(Long customerId) {
        Wishlist wishlist = getOrCreateWishlist(customerId);
        return WishlistDto.fromEntity(wishlist);
    }
}
