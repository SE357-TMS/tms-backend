package com.example.tms.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tms.enity.PasswordResetToken;
import com.example.tms.enity.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByUserAndUsedFalseAndExpiryDateAfter(User user, LocalDateTime now);
    
    void deleteByExpiryDateBefore(LocalDateTime now);
    
    void deleteByUser(User user);
}
