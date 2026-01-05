package com.example.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.example.backend.security.auth.RestAuthenticationEntryPoint;
import com.example.backend.security.auth.TokenAuthenticationFilter;
import com.example.backend.service.CustomUserDetailsService;
import com.example.backend.util.TokenUtils;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig {

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private TokenUtils tokenUtils;

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Stateless session management
        http.sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Handle unauthorized requests
        http.exceptionHandling(exception -> 
            exception.authenticationEntryPoint(restAuthenticationEntryPoint));

        // Configure URL access
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()        // Public endpoints
            .requestMatchers("/h2-console/**").permitAll()  // H2 console (if used)
            .requestMatchers("/api/foo").permitAll()        // Example public endpoint
            .requestMatchers(
            		"/signup",
                    "/activate",
                    "/auth/**",
                    "/h2-console/**",
                    "/api/foo",
                    "/favicon.ico",
                    "/webjars/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/static/**"
            ).permitAll()                                    // Static resources
            .anyRequest().authenticated()                    // All other requests require auth
        );

        // CORS configuration
        http.cors(cors -> cors.configure(http));

        // Disable CSRF for REST API
        http.csrf(csrf -> csrf.disable());

        // Add custom JWT filter
        http.addFilterBefore(
            new TokenAuthenticationFilter(tokenUtils, userDetailsService()),
            BasicAuthenticationFilter.class
        );

        // Set authentication provider
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}
