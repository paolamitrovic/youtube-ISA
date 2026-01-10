package com.example.backend.security.auth;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.backend.model.User;
import com.example.backend.util.TokenUtils;

import io.jsonwebtoken.ExpiredJwtException;


public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private TokenUtils tokenUtils;
    private UserDetailsService userDetailsService;
    
    protected final Log LOGGER = LogFactory.getLog(getClass());
 
    // Lista javnih endpointa koji ne trebaju token validaciju
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/auth/login",
            "/auth/signup",
            "/auth/activate",
            "/h2-console"
        );
    
    public TokenAuthenticationFilter(TokenUtils tokenHelper, UserDetailsService userDetailsService) {
        this.tokenUtils = tokenHelper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
    	
    	String requestPath = request.getRequestURI();  // DODAJ OVU LINIJU
        
        // Preskoči token validaciju za javne endpointe
        if (isPublicEndpoint(requestPath)) {
            chain.doFilter(request, response);
            return;
        }
        
        String email;
        
        // 1. Extract JWT token from request
        String authToken = tokenUtils.getToken(request);
        
        try {
            if (authToken != null) {
                
                // 2. Extract email from token
                email = tokenUtils.getEmailFromToken(authToken);
                
                if (email != null) {
                    
                    // 3. Load user details from database
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    
                    // 4. Validate token
                    if (tokenUtils.validateToken(authToken, userDetails)) {
                        
                        // 5. Create authentication and set in SecurityContext
                        TokenBasedAuthentication authentication = new TokenBasedAuthentication(userDetails);
                        authentication.setToken(authToken);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
            
        } catch (ExpiredJwtException ex) {
            LOGGER.debug("Token expired!");
        } 
        
        // Continue filter chain
        chain.doFilter(request, response);
    }
    
    private boolean isPublicEndpoint(String requestPath) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(endpoint -> requestPath.startsWith(endpoint));
    }
}
