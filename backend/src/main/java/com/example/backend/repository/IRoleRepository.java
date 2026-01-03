package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.model.Role;

public interface IRoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByName(String name);
}
