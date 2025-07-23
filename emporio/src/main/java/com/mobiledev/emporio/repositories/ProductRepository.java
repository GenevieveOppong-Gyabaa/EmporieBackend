package com.mobiledev.emporio.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mobiledev.emporio.model.Product;
import com.mobiledev.emporio.model.User;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
    void deleteById(Long id);
    List<Product> findBySeller(User seller);
    List<Product> findByNameContainingIgnoreCase(String keyword);
    List<Product> findByOnDealTrue();
    List<Product> findByDiscountPriceNotNull();
    Page<Product> findBySeller(User seller, Pageable pageable);
}
