package com.mobiledev.emporio.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne
private User buyer;

@ManyToOne
private User seller;

@ManyToMany
private List<Product> products;

private String status;

private LocalDateTime orderDate;

private String paymentStatus;

@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
private List<OrderItem> items;

private String shippingAddress;

private String paymentMethod;

private Double total;

public Long getId() {
    return id;  

}
public void setId(Long id) {
    this.id = id;
}

public User getBuyer() {
    return buyer;

}
public void setBuyer(User buyer) {
    this.buyer = buyer;
}
public User getSeller() {
    return seller;
}
public void setSeller(User seller) {
    this.seller = seller;
}
public List<Product> getProducts() {
    return products;
}

public void setProducts(List<Product> products) {
    this.products = products;
}
public String getStatus() {
    return status;
}
public void setStatus(String status) {
    this.status = status;
}
public LocalDateTime getOrderDate() {
    return orderDate;
}
public void setOrderDate(LocalDateTime orderDate) {
    this.orderDate = orderDate;
}

public String getPaymentStatus() {
    return paymentStatus;
}
public void setPaymentStatus(String paymentStatus) {
    this.paymentStatus = paymentStatus;
}

public List<OrderItem> getItems() {
    return items;
}
public void setItems(List<OrderItem> items) {
    this.items = items;
}
public String getShippingAddress() {
    return shippingAddress;
}
public void setShippingAddress(String shippingAddress) {
    this.shippingAddress = shippingAddress;
}
public String getPaymentMethod() {
    return paymentMethod;
}
public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
}
public Double getTotal() {
    return total;
}
public void setTotal(Double total) {
    this.total = total;
}

}
