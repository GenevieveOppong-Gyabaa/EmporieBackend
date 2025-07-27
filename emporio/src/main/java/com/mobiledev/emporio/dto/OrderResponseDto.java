package com.mobiledev.emporio.dto;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.mobiledev.emporio.model.Order;

public class OrderResponseDto {
    private Long id;
    private String title;
    private String date;
    private String status;
    private Double total;
    private String paymentStatus;
    private List<String> productNames;

    public OrderResponseDto() {}

    public OrderResponseDto(Order order) {
        this.id = order.getId();
        this.title = "Order #" + order.getId();
        this.date = order.getOrderDate() != null ? 
            order.getOrderDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "";
        this.status = order.getStatus() != null ? order.getStatus() : "PENDING";
        this.total = order.getTotal();
        this.paymentStatus = order.getPaymentStatus();
        
        // Extract product names
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            this.productNames = order.getItems().stream()
                .map(item -> item.getProduct() != null ? item.getProduct().getName() : "Unknown Product")
                .collect(Collectors.toList());
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public List<String> getProductNames() { return productNames; }
    public void setProductNames(List<String> productNames) { this.productNames = productNames; }
} 