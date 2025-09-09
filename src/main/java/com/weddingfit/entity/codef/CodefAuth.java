package com.weddingfit.entity.codef;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "codef_auth")
public class CodefAuth {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "access_token", columnDefinition = "TEXT", nullable = false)
    private String accessToken;
    
    @Column(name = "token_expires_at", nullable = false)
    private LocalDateTime tokenExpiresAt;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 기본 생성자
    public CodefAuth() {}
    
    // 생성자 (토큰 발급 시 +6일로 만료일 설정)
    public CodefAuth(String accessToken) {
        this.accessToken = accessToken;
        this.tokenExpiresAt = LocalDateTime.now().plusDays(6); // codef 토큰은 6일 유효
    }
    
    // 토큰이 만료되었는지 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(tokenExpiresAt);
    }
    
    // 토큰이 하루 내에 만료되는지 확인 (갱신 알림용)
    public boolean isExpiringWithinOneDay() {
        return LocalDateTime.now().plusDays(1).isAfter(tokenExpiresAt);
    }
    
    // 토큰 갱신
    public void renewToken(String newAccessToken) {
        this.accessToken = newAccessToken;
        this.tokenExpiresAt = LocalDateTime.now().plusDays(6);
        this.isActive = true;
    }
    
    // 토큰 비활성화
    public void deactivate() {
        this.isActive = false;
    }
    
    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public LocalDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}