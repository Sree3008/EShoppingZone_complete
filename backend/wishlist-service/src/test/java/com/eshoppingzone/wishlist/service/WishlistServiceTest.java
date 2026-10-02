package com.eshoppingzone.wishlist.service;

import com.eshoppingzone.wishlist.client.ProductClient;
import com.eshoppingzone.wishlist.dto.*;
import com.eshoppingzone.wishlist.entity.Wishlist;
import com.eshoppingzone.wishlist.entity.WishlistItem;
import com.eshoppingzone.wishlist.exception.ResourceNotFoundException;
import com.eshoppingzone.wishlist.repository.WishlistItemRepository;
import com.eshoppingzone.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private Wishlist sampleWishlist;

    @BeforeEach
    void setUp() {
        sampleWishlist = new Wishlist(1L, 101L);
        sampleWishlist.setItems(new ArrayList<>());
    }

    @Test
    void testGetMyWishlist_ExistingWishlist() {
        when(wishlistRepository.findByCustomerId(101L)).thenReturn(Optional.of(sampleWishlist));

        WishlistDto result = wishlistService.getMyWishlist(101L);

        assertNotNull(result);
        assertEquals(101L, result.getCustomerId());
        assertEquals(0, result.getTotalItems());
        verify(wishlistRepository, times(1)).findByCustomerId(101L);
    }

    @Test
    void testGetMyWishlist_CreatesWhenNotExists() {
        when(wishlistRepository.findByCustomerId(101L)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(sampleWishlist);

        WishlistDto result = wishlistService.getMyWishlist(101L);

        assertNotNull(result);
        assertEquals(101L, result.getCustomerId());
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
    }

    @Test
    void testAddToWishlist_Success() {
        when(wishlistRepository.findByCustomerId(101L)).thenReturn(Optional.of(sampleWishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductSnapshotDto product = new ProductSnapshotDto(5L, 2L, "Test Laptop", BigDecimal.valueOf(999.99), "Electronics", "http://img.jpg", "ACTIVE");
        when(productClient.getProductById(5L)).thenReturn(ApiResponse.success(product));

        AddToWishlistRequest request = new AddToWishlistRequest(5L);
        WishlistDto result = wishlistService.addToWishlist(101L, request);

        assertNotNull(result);
        assertEquals(1, result.getTotalItems());
        assertEquals("Test Laptop", result.getItems().get(0).getProductName());
        assertEquals(BigDecimal.valueOf(999.99), result.getItems().get(0).getUnitPrice());
    }

    @Test
    void testAddToWishlist_IdempotentWhenAlreadyPresent() {
        WishlistItem existingItem = new WishlistItem(10L, sampleWishlist, 5L, "Test Laptop", BigDecimal.valueOf(999.99));
        sampleWishlist.getItems().add(existingItem);

        when(wishlistRepository.findByCustomerId(101L)).thenReturn(Optional.of(sampleWishlist));

        AddToWishlistRequest request = new AddToWishlistRequest(5L);
        WishlistDto result = wishlistService.addToWishlist(101L, request);

        assertNotNull(result);
        assertEquals(1, result.getTotalItems());
        verify(productClient, never()).getProductById(any());
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void testRemoveFromWishlist_Success() {
        WishlistItem existingItem = new WishlistItem(10L, sampleWishlist, 5L, "Test Laptop", BigDecimal.valueOf(999.99));
        sampleWishlist.getItems().add(existingItem);

        when(wishlistRepository.findByCustomerId(101L)).thenReturn(Optional.of(sampleWishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WishlistDto result = wishlistService.removeFromWishlist(101L, 5L);

        assertNotNull(result);
        assertEquals(0, result.getTotalItems());
    }

    @Test
    void testRemoveFromWishlist_NotFound() {
        when(wishlistRepository.findByCustomerId(101L)).thenReturn(Optional.of(sampleWishlist));

        assertThrows(ResourceNotFoundException.class, () -> {
            wishlistService.removeFromWishlist(101L, 999L);
        });
    }

    @Test
    void testIsProductInWishlist() {
        WishlistItem existingItem = new WishlistItem(10L, sampleWishlist, 5L, "Test Laptop", BigDecimal.valueOf(999.99));
        sampleWishlist.getItems().add(existingItem);

        when(wishlistRepository.findByCustomerId(101L)).thenReturn(Optional.of(sampleWishlist));

        WishlistCheckResponse present = wishlistService.isProductInWishlist(101L, 5L);
        assertTrue(present.isInWishlist());

        WishlistCheckResponse notPresent = wishlistService.isProductInWishlist(101L, 999L);
        assertFalse(notPresent.isInWishlist());
    }

    @Test
    void testClearWishlist() {
        WishlistItem existingItem = new WishlistItem(10L, sampleWishlist, 5L, "Test Laptop", BigDecimal.valueOf(999.99));
        sampleWishlist.getItems().add(existingItem);

        when(wishlistRepository.findByCustomerId(101L)).thenReturn(Optional.of(sampleWishlist));

        wishlistService.clearWishlist(101L);

        assertTrue(sampleWishlist.getItems().isEmpty());
        verify(wishlistRepository, times(1)).save(sampleWishlist);
    }
}
