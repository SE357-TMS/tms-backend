package com.example.tms.service.impl;

import com.example.tms.enity.PasswordResetToken;
import com.example.tms.enity.User;
import com.example.tms.repository.PasswordResetTokenRepository;
import com.example.tms.repository.UserRepository;
import com.example.tms.service.interface_.EmailService;
import com.example.tms.service.interface_.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private static final int TOKEN_EXPIRY_MINUTES = 15;

    @Override
    @Transactional
    public void sendPasswordResetEmail(String email) {
        // Find user by email
        User user = userRepository.findByUsername(email)
                .or(() -> userRepository.findByEmail(email))
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if user is deleted or locked
        if (user.isDeleted()) {
            throw new RuntimeException("Cannot reset password for deleted account");
        }
        if (user.getIsLock()) {
            throw new RuntimeException("Account is locked. Please contact administrator");
        }

        // Delete old unused tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate new token
        String token = UUID.randomUUID().toString();
        
        // Create and save reset token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
        resetToken.setUsed(false);
        resetToken.setCreatedAt(LocalDateTime.now());
        
        tokenRepository.save(resetToken);

        // Build reset URL
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        // Send email
        try {
            emailService.sendPasswordResetEmail(
                user.getEmail(), 
                user.getFullName() != null ? user.getFullName() : user.getUsername(),
                token,
                resetUrl
            );
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Find token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Validate token
        if (resetToken.isUsed()) {
            throw new RuntimeException("This reset token has already been used");
        }
        if (resetToken.isExpired()) {
            throw new RuntimeException("This reset token has expired");
        }

        // Get user
        User user = resetToken.getUser();
        
        // Check if user is deleted or locked
        if (user.isDeleted()) {
            throw new RuntimeException("Cannot reset password for deleted account");
        }
        if (user.getIsLock()) {
            throw new RuntimeException("Account is locked. Please contact administrator");
        }

        // Update password
        user.setUserPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // Send confirmation email
        try {
            emailService.sendPasswordChangedEmail(
                user.getEmail(),
                user.getFullName() != null ? user.getFullName() : user.getUsername()
            );
            log.info("Password changed successfully for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to send password changed confirmation email", e);
            // Don't throw exception here, password was already changed successfully
        }
    }

    @Override
    public boolean validateToken(String token) {
        return tokenRepository.findByToken(token)
                .map(resetToken -> !resetToken.isUsed() && !resetToken.isExpired())
                .orElse(false);
    }
}
