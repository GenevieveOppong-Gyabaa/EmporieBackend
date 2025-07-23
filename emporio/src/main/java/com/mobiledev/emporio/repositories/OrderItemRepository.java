package com.mobiledev.emporio.repositories;

import com.mobiledev.emporio.model.OrderItem;
import com.mobiledev.emporio.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    long countByProductIn(List<Product> products);

    @Query("SELECT SUM(oi.subtotal) FROM OrderItem oi WHERE oi.product IN :products")
    Double sumEarningsByProductIn(@Param("products") List<Product> products);
} 