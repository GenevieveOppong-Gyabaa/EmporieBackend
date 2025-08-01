package com.mobiledev.emporio.dto;

public class CartItemResponse {
    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private Double total;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
} 