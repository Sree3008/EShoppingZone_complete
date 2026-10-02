package com.eshoppingzone.wishlist.controller;

import com.eshoppingzone.wishlist.dto.*;
import com.eshoppingzone.wishlist.exception.GlobalExceptionHandler;
import com.eshoppingzone.wishlist.exception.ResourceNotFoundException;
import com.eshoppingzone.wishlist.security.UserPrincipal;
import com.eshoppingzone.wishlist.service.WishlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WishlistControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WishlistService wishlistService;

    @InjectMocks
    private WishlistController wishlistController;

    private ObjectMapper objectMapper;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wishlistController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        UserPrincipal principal = new UserPrincipal(101L, "customer1", "customer1@eshoppingzone.com", "CUSTOMER");
        auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetMyWishlist_Returns200() throws Exception {
        WishlistDto dto = new WishlistDto(1L, 101L, Collections.emptyList(), 0, LocalDateTime.now());
        when(wishlistService.getMyWishlist(101L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/wishlist").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value(101));
    }

    @Test
    void testAddToWishlist_Returns201() throws Exception {
        WishlistItemDto item = new WishlistItemDto(1L, 5L, "Phone", BigDecimal.valueOf(499.99), "Tech", null, LocalDateTime.now());
        WishlistDto dto = new WishlistDto(1L, 101L, Collections.singletonList(item), 1, LocalDateTime.now());

        when(wishlistService.addToWishlist(eq(101L), any(AddToWishlistRequest.class))).thenReturn(dto);

        AddToWishlistRequest req = new AddToWishlistRequest(5L);

        mockMvc.perform(post("/api/v1/wishlist/items")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.items[0].productName").value("Phone"));
    }

    @Test
    void testRemoveFromWishlist_Returns200() throws Exception {
        WishlistDto dto = new WishlistDto(1L, 101L, Collections.emptyList(), 0, LocalDateTime.now());
        when(wishlistService.removeFromWishlist(101L, 5L)).thenReturn(dto);

        mockMvc.perform(delete("/api/v1/wishlist/items/5").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testRemoveFromWishlist_NotFound_Returns404() throws Exception {
        when(wishlistService.removeFromWishlist(101L, 999L))
                .thenThrow(new ResourceNotFoundException("Product with id 999 is not in the wishlist"));

        mockMvc.perform(delete("/api/v1/wishlist/items/999").principal(auth))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product with id 999 is not in the wishlist"));
    }

    @Test
    void testCheckProductInWishlist_Returns200() throws Exception {
        WishlistCheckResponse resp = new WishlistCheckResponse(5L, true);
        when(wishlistService.isProductInWishlist(101L, 5L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/wishlist/check/5").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(5))
                .andExpect(jsonPath("$.data.inWishlist").value(true));
    }

    @Test
    void testClearWishlist_Returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/wishlist").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Wishlist cleared successfully"));
    }

    @Test
    void testGetWishlistByCustomerId_InternalEndpoint() throws Exception {
        WishlistDto dto = new WishlistDto(1L, 101L, Collections.emptyList(), 0, LocalDateTime.now());
        when(wishlistService.getWishlistByCustomerId(101L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/wishlist/customer/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value(101));
    }
}
