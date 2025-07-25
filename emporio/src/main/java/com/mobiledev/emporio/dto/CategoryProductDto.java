package com.mobiledev.emporio.dto;

public class CategoryProductDto {
    private Long id;
    private String name;
    private String image;
    private String price;
    private String discountPrice;
    private String description;
    private Boolean inStock;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(String discountPrice) { this.discountPrice = discountPrice; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }
} 