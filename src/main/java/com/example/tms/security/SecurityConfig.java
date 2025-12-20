package com.example.tms.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtBlackListFilter jwtBlackListFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, JwtBlackListFilter jwtBlackListFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtBlackListFilter = jwtBlackListFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/api/v1/images/users/*/avatar").permitAll() // Public access to view avatars
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll() // Swagger UI
                    // Customer public APIs (no auth required for viewing)
                    .requestMatchers("/api/v1/customer/tours/suggestions").permitAll()
                    .requestMatchers("/api/v1/customer/tours/home").permitAll()
                    .requestMatchers("/api/v1/customer/tours/home/destination-images").permitAll()
                    .requestMatchers("/api/v1/customer/tours/search").permitAll()
                    .requestMatchers("/api/v1/customer/tours/start-locations").permitAll()
                    .requestMatchers("/api/v1/customer/tours/*/favorite").permitAll() // GET is public, POST requires auth in controller
                    .requestMatchers("/api/v1/routes/*/detail").permitAll() // Route detail page
                    .requestMatchers("/api/v1/routes/*").permitAll() // Route info
                    .requestMatchers("/api/v1/trips/route/*/available").permitAll() // Available trips
                    .requestMatchers("/api/v1/trips/route/*/nearest").permitAll() // Nearest trip
                    .requestMatchers("/api/v1/images/routes/*/images").permitAll() // Route gallery
                    .requestMatchers("/admin/**").hasAuthority("ADMIN")
                    .requestMatchers("/staff/**").hasAuthority("STAFF")
                    .requestMatchers("/customer/**").hasAuthority("CUSTOMER")
                    .anyRequest().authenticated()
            )
            // Register both custom filters before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtBlackListFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}

