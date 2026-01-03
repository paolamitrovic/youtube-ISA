package com.example.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.model.Role;
import com.example.backend.repository.IRoleRepository;

@Service
public class RoleService {
	
	@Autowired
    private IRoleRepository roleRepository;

    public Role findById(Long id) {
        return this.roleRepository.findById(id).orElse(null);
    }

    public List<Role> findByName(String name) {
        List<Role> roles = this.roleRepository.findByName(name);
        return roles;
    }

}
