package com.example.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.JwtAuthenticationRequest;
import com.example.backend.dto.UserRequest;
import com.example.backend.dto.UserTokenState;
import com.example.backend.exception.ResourceConflictException;
import com.example.backend.model.User;
import com.example.backend.service.EmailService;
import com.example.backend.service.LoginAttemptService;
import com.example.backend.service.UserService;
import com.example.backend.util.TokenUtils;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private LoginAttemptService loginAttemptService;

    
    @PostMapping("/login")
    public ResponseEntity<UserTokenState> createAuthenticationToken(
            @RequestBody JwtAuthenticationRequest authenticationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        // Dobavljanje IP adrese klijenta
        String ip = getClientIP(request);

        // ⛔ Provera da li je IP adresa privremeno blokirana
        if (loginAttemptService.isBlocked(ip)) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new UserTokenState(null, 0));
        }

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getEmail(),
                    authenticationRequest.getPassword()
                )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            User user = (User) authentication.getPrincipal();

            // Check if account is active
            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new UserTokenState(null, 0));
            }

            // ✅ Uspešna prijava → reset broja neuspešnih pokušaja za IP
            loginAttemptService.loginSucceeded(ip);

            String jwt = tokenUtils.generateToken(user.getEmail());
            int expiresIn = tokenUtils.getExpiredIn();

            // Return token
            return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));

        } catch (Exception e) {
            // ❌ Neuspešna prijava → povećaj broj pokušaja za IP
            loginAttemptService.loginFailed(ip);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new UserTokenState(null, 0));
        }
    }

    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }


    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> addUser(@RequestBody UserRequest userRequest) {
        User existUser = this.userService.findByEmail(userRequest.getEmail());

        if (existUser != null) {
        	throw new ResourceConflictException(userRequest.getId(), "E-mail adresa već postoji");
        }

        User user = this.userService.save(userRequest);
        
        // Send activation email asynchronously
        try {
            emailService.sendActivationEmail(user.getEmail(), user.getActivationToken().getToken());
        } catch (Exception e) {
            // Log error but don't fail signup
            System.err.println("Došlo je do greške prilikom slanja email-a: " + e.getMessage());
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Registracija uspešna! Molim Vas proverite email za aktivaciju.");
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/activate")
    public ResponseEntity<?> activateAccount(@RequestParam String token) {
        try {
        	userService.activateAccount(token);
            return ResponseEntity.ok("Nalog je uspešno aktiviran. Možete sada da se prijavite.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Activation failed: " + e.getMessage());
        }
    }
}
