package com.eshoppingzone.wishlist.dto;

import com.eshoppingzone.wishlist.entity.Wishlist;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WishlistDto {

    private Long id;
    private Long customerId;
    private List<WishlistItemDto> items = new ArrayList<>();
    private int totalItems;
    private LocalDateTime updatedAt;

    public WishlistDto() {
    }

    public WishlistDto(Long id, Long customerId, List<WishlistItemDto> items, int totalItems, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.totalItems = totalItems;
        this.updatedAt = updatedAt;
    }

    public static WishlistDto fromEntity(Wishlist wishlist) {
        if (wishlist == null) return null;

        List<WishlistItemDto> itemDtos = wishlist.getItems() != null
                ? wishlist.getItems().stream().map(WishlistItemDto::fromEntity).collect(Collectors.toList())
                : new ArrayList<>();

        return new WishlistDto(
                wishlist.getId(),
                wishlist.getCustomerId(),
                itemDtos,
                itemDtos.size(),
                wishlist.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<WishlistItemDto> getItems() {
        return items;
    }

    public void setItems(List<WishlistItemDto> items) {
        this.items = items;
        this.totalItems = items != null ? items.size() : 0;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
