package com.mobiledev.emporio.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobiledev.emporio.model.Cart;
import com.mobiledev.emporio.model.CartItem;
import com.mobiledev.emporio.model.Product;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    long countByProductIn(List<Product> products);
}
