package com.example.tms.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.entity.RefreshToken;
import com.example.tms.entity.UnusedAccessToken;
import com.example.tms.repository.RefreshTokenRepository;
import com.example.tms.repository.UnusedAccessTokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    // Base64-encoded secret key (HS256)
    @Value("${app.security.jwt.secret}")
    private String secretBase64;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UnusedAccessTokenRepository unusedAccessTokenRepository;

    // 30 minutes access token; 24 hours refresh token
    private static final long ACCESS_TOKEN_TTL_MS = 30L * 60 * 1000;
    private static final long REFRESH_TOKEN_TTL_MS = 24L * 60 * 60 * 1000;

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Add roles and fullName as claims
        if (!userDetails.getAuthorities().isEmpty()) {
            claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        }
        // You can add fullName here if User entity is passed
        claims.put("fullName", userDetails.getUsername()); // Default to username, update if needed
        return createToken(claims, userDetails.getUsername());
    }

    public String generateRefreshToken(String username) {
        String refreshToken = createRefreshToken(new HashMap<>(), username);
        RefreshToken rt = new RefreshToken();
        rt.setToken(refreshToken);
        rt.setExpireAt(extractExpiration(refreshToken));
        refreshTokenRepository.save(rt);
        return refreshToken;
    }

    public boolean validateRefreshToken(String token, UserDetails userDetails) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.getExpireAt().before(new Date()))
                .map(rt -> extractUsername(token).equals(userDetails.getUsername()))
                .orElse(false);
    }

    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    public void saveUnusedAccessToken(String accessToken) {
        UnusedAccessToken uat = new UnusedAccessToken();
        uat.setToken(accessToken);
        uat.setExpireAt(extractExpiration(accessToken));
        unusedAccessTokenRepository.save(uat);
    }

    public UnusedAccessToken getUnusedAccessTokenByToken(String token) {
        return unusedAccessTokenRepository.findByToken(token).orElse(null);
    }

    public List<UnusedAccessToken> getAllAccessTokenExpired() {
        return unusedAccessTokenRepository.findByExpireAtBefore(new Date());
    }

    @Transactional
    public void deleteAllExpiredBlacklistedTokens() {
        List<UnusedAccessToken> list = getAllAccessTokenExpired();
        unusedAccessTokenRepository.deleteAll(list);
    }

    @Transactional
    public void deleteAllExpiredRefreshTokens() {
        var list = refreshTokenRepository.findByExpireAtBefore(new Date());
        refreshTokenRepository.deleteAll(list);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ACCESS_TOKEN_TTL_MS))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String createRefreshToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + REFRESH_TOKEN_TTL_MS))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
