package com.mobiledev.emporio.dto;

public class SellerDashboardDto {
    private int productsListed;
    private long inCarts;
    private long itemsSold;
    private double earnings;

    public SellerDashboardDto(int productsListed, long inCarts, long itemsSold, double earnings) {
        this.productsListed = productsListed;
        this.inCarts = inCarts;
        this.itemsSold = itemsSold;
        this.earnings = earnings;
    }

    public int getProductsListed() { return productsListed; }
    public void setProductsListed(int productsListed) { this.productsListed = productsListed; }
    public long getInCarts() { return inCarts; }
    public void setInCarts(long inCarts) { this.inCarts = inCarts; }
    public long getItemsSold() { return itemsSold; }
    public void setItemsSold(long itemsSold) { this.itemsSold = itemsSold; }
    public double getEarnings() { return earnings; }
    public void setEarnings(double earnings) { this.earnings = earnings; }
} 