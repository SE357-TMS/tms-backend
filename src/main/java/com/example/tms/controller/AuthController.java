package com.example.tms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tms.dto.request.LoginRequest;
import com.example.tms.dto.request.LogoutRequest;
import com.example.tms.dto.request.RefreshTokenRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.dto.response.JwtResponse;
import com.example.tms.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails.getUsername());
            return ResponseEntity.ok(
                    ApiResponse.success("Login successful", new JwtResponse(token, refreshToken))
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid username or password"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        try {
            String username = jwtService.extractUsername(refreshToken);
            
            // Load real user from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (!jwtService.validateRefreshToken(refreshToken, userDetails)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid or expired refresh token"));
            }
            
            // Chỉ tạo access token mới, GIỮ NGUYÊN refresh token cũ
            String newAccess = jwtService.generateToken(userDetails);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Token refreshed successfully", new JwtResponse(newAccess, refreshToken))
            );
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Failed to refresh token: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request, HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            jwtService.saveUnusedAccessToken(accessToken);
        }
        if (request.getRefreshToken() != null) {
            jwtService.deleteRefreshToken(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }
}
