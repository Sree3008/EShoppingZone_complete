package com.eshoppingzone.wishlist.dto;

import com.eshoppingzone.wishlist.entity.WishlistItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WishlistItemDto {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private String category;
    private String imageUrl;
    private LocalDateTime addedAt;

    public WishlistItemDto() {
    }

    public WishlistItemDto(Long id, Long productId, String productName, BigDecimal unitPrice, String category, String imageUrl, LocalDateTime addedAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.category = category;
        this.imageUrl = imageUrl;
        this.addedAt = addedAt;
    }

    public static WishlistItemDto fromEntity(WishlistItem item) {
        if (item == null) return null;
        return new WishlistItemDto(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getCategory(),
                item.getImageUrl(),
                item.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getPrice() {
        return unitPrice;
    }

    public void setPrice(BigDecimal price) {
        this.unitPrice = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
