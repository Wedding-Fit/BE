package com.weddingfit.repository.codef;

import com.weddingfit.entity.codef.CodefAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CodefAuthRepository extends JpaRepository<CodefAuth, Long> {
    
    // 활성화된 토큰 중 가장 최근 것 조회
    Optional<CodefAuth> findFirstByIsActiveTrueOrderByCreatedAtDesc();
    
    // 만료되지 않은 활성 토큰 조회
    @Query("SELECT c FROM CodefAuth c WHERE c.isActive = true AND c.tokenExpiresAt > :now ORDER BY c.createdAt DESC")
    Optional<CodefAuth> findValidToken(LocalDateTime now);
    
    // 하루 내 만료 예정인 토큰들 조회
    @Query("SELECT c FROM CodefAuth c WHERE c.isActive = true AND c.tokenExpiresAt BETWEEN :now AND :oneDayLater")
    List<CodefAuth> findTokensExpiringWithinOneDay(LocalDateTime now, LocalDateTime oneDayLater);
    
    // 만료된 토큰들 조회 (정리용)
    @Query("SELECT c FROM CodefAuth c WHERE c.tokenExpiresAt < :now")
    List<CodefAuth> findExpiredTokens(LocalDateTime now);
}