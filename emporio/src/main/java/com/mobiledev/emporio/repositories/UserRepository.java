package com.mobiledev.emporio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobiledev.emporio.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
}