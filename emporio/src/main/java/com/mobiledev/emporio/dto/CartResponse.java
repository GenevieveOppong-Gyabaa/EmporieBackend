package com.mobiledev.emporio.dto;

import java.util.List;

public class CartResponse {
    private Long userId;
    private List<CartItemResponse> items;
    private Double totalPrice;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<CartItemResponse> getItems() { return items; }
    public void setItems(List<CartItemResponse> items) { this.items = items; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
} 