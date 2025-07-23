package com.mobiledev.emporio.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobiledev.emporio.model.Order;
import com.mobiledev.emporio.model.User;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyer(User buyer);
    List<Order> findBySeller(User seller);
    List<Order> findByStatus(String status);
}
