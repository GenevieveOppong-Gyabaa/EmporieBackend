package com.mobiledev.emporio.dto;

public class SellerProductDto {
    private Long id;
    private String name;
    private int views;
    private long carts;
    private long sold;

    public SellerProductDto(Long id, String name, int views, long carts, long sold) {
        this.id = id;
        this.name = name;
        this.views = views;
        this.carts = carts;
        this.sold = sold;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
    public long getCarts() { return carts; }
    public void setCarts(long carts) { this.carts = carts; }
    public long getSold() { return sold; }
    public void setSold(long sold) { this.sold = sold; }
} 