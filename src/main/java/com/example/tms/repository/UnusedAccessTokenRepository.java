package com.example.tms.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tms.enity.UnusedAccessToken;

public interface UnusedAccessTokenRepository extends JpaRepository<UnusedAccessToken, UUID> {
    Optional<UnusedAccessToken> findByToken(String token);
    List<UnusedAccessToken> findByExpireAtBefore(Date expireAt);
}
