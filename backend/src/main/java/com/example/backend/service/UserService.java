package com.example.backend.service;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.dto.UserRequest;
import com.example.backend.model.ActivationToken;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.repository.IActivationTokenRepository;
import com.example.backend.repository.IUserRepository;
import com.example.backend.util.TokenGenerator;

@Service
public class UserService {
	
	@Autowired
	private IUserRepository userRepository;
	
	@Autowired
	private IActivationTokenRepository tokenRepository;
	
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
        
        u.setFirstName(userRequest.getFirstName());
        u.setLastName(userRequest.getLastName());
        u.setEmail(userRequest.getEmail());
        u.setAddress(userRequest.getAddress());
        
        // Account starts as disabled - must be activated via email
        u.setActive(false);
        
        // Generate activation token entity
        ActivationToken token = new ActivationToken();
        token.setToken(TokenGenerator.generateActivationToken());
        token.setUser(u);
        token.setExpiryDate(LocalDateTime.now().plusHours(24)); // 24h validnost

        u.setActivationToken(token);

        // Assign default role (ROLE_USER)
        List<Role> roles = roleService.findByName("ROLE_USER");
        u.setRoles(roles);
        
        return this.userRepository.save(u);
    }
    
    public User activateAccount(String tokenString) {
        // Find token in base
        ActivationToken token = tokenRepository.findByToken(tokenString);

        if (token == null) {
            throw new RuntimeException("Invalid activation token");
        }

        // Check if it's expired
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Activation token has expired");
        }

        // Activate user
        User user = token.getUser();
        user.setActive(true);

        // Delete token after activation
        user.setActivationToken(null);
        tokenRepository.delete(token);

        return userRepository.save(user);
    }

    
    public User findByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null);
	} 
    
    public User update(User user) {
        return userRepository.save(user);
    }
}
