package com.example.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.dto.UserRequest;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.repository.IUserRepository;

@Service
public class UserService {
	
	@Autowired
	private IUserRepository userRepository;
	
	@Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;
	
	public User findByUsername(String username) {
		return userRepository.findByUsername(username).orElseThrow();
	}
	
	public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User save(UserRequest userRequest) {
        User u = new User();
        u.setUsername(userRequest.getUsername());
        
        // Hash password before saving
        u.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        
        u.setFirstName(userRequest.getFirstname());
        u.setLastName(userRequest.getLastname());
        u.setEmail(userRequest.getEmail());
        u.setAddress(userRequest.getAddress());

        // Assign default role (ROLE_USER)
        List<Role> roles = roleService.findByName("ROLE_USER");
        u.setRoles(roles);
        
        return this.userRepository.save(u);
    }
    
    public User findByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null);
	} 
}
