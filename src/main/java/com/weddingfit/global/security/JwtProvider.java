package com.weddingfit.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {
    
    @Value("${jwt.secret-key}")
    private String secretKey;
    
    @Value("${jwt.access-token-expire-time}")
    private long accessTokenExpireTime;
    
    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpireTime;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    
    public String createAccessToken(Long userId, String nickname, Long coupleId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpireTime, ChronoUnit.MILLIS);
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("nickname", nickname)
                .claim("coupleId", coupleId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    public Long getUserId(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }
    
    public String getNickname(String token) {
        return getClaims(token).get("nickname", String.class);
    }
    
    public Long getCoupleId(String token) {
        return getClaims(token).get("coupleId", Long.class);
    }
    
    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenExpireTime, ChronoUnit.MILLIS);
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }
}