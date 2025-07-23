package com.mobiledev.emporio.repositories;

import com.mobiledev.emporio.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
} 