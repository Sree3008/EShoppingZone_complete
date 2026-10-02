package com.eshoppingzone.wishlist.dto;

import java.math.BigDecimal;

public class ProductSnapshotDto {
    private Long id;
    private Long merchantId;
    private String name;
    private BigDecimal price;
    private String category;
    private String imageUrl;
    private String status;

    public ProductSnapshotDto() {
    }

    public ProductSnapshotDto(Long id, Long merchantId, String name, BigDecimal price, String category, String imageUrl, String status) {
        this.id = id;
        this.merchantId = merchantId;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
