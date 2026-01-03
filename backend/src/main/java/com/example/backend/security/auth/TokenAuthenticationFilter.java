package com.example.backend.security.auth;

import java.io.IOException;
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

    public TokenAuthenticationFilter(TokenUtils tokenHelper, UserDetailsService userDetailsService) {
        this.tokenUtils = tokenHelper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

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
}
