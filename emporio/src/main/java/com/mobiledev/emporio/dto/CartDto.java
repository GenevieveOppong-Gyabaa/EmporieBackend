package com.mobiledev.emporio.dto;

import java.util.List;

public class CartDto {
    private List<CartItemDto> items;
    private double totalPrice;

    // getters/setters

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}