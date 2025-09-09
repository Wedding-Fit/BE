package com.weddingfit.service.codef;

import com.weddingfit.entity.codef.CodefAuth;
import com.weddingfit.repository.codef.CodefAuthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CodefAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(CodefAuthService.class);
    
    private final CodefAuthRepository codefAuthRepository;
    private final CodefAuthClient codefAuthClient;
    
    @Autowired
    public CodefAuthService(CodefAuthRepository codefAuthRepository, CodefAuthClient codefAuthClient) {
        this.codefAuthRepository = codefAuthRepository;
        this.codefAuthClient = codefAuthClient;
    }
    
    /**
     * 유효한 access_token 조회 (만료 시 자동 갱신)
     * @return 유효한 access_token
     */
    @Transactional
    public String getValidAccessToken() {
        logger.debug("유효한 access_token 조회 시작");
        
        // 1. DB에서 유효한 토큰 조회
        Optional<CodefAuth> validToken = codefAuthRepository.findValidToken(LocalDateTime.now());
        
        if (validToken.isPresent()) {
            logger.debug("DB에서 유효한 토큰 발견: 만료일={}", validToken.get().getTokenExpiresAt());
            return validToken.get().getAccessToken();
        }
        
        // 2. 유효한 토큰이 없으면 새로 발급
        logger.info("유효한 토큰이 없어서 새로 발급 요청");
        return issueNewToken();
    }
    
    /**
     * 새로운 토큰 발급 및 저장
     * @return 새로 발급된 access_token
     */
    @Transactional(rollbackFor = Exception.class)
    public String issueNewToken() {
        try {
            // 1. codef API를 통해 새 토큰 발급
            CodefAuthClient.CodefTokenResponse response = codefAuthClient.getAccessToken();
            
            if (response == null || response.getAccess_token() == null) {
                logger.error("codef 토큰 발급 실패: 응답이 null입니다");
                throw new CodefTokenException("codef 토큰 발급 실패: 응답이 null입니다");
            }
            
            // 2. 기존 토큰들 비활성화
            deactivateAllTokens();
            
            // 3. 새 토큰 저장
            CodefAuth newToken = new CodefAuth(response.getAccess_token());
            codefAuthRepository.save(newToken);
            
            logger.info("새 토큰 발급 완료: 만료일={}", newToken.getTokenExpiresAt());
            return newToken.getAccessToken();
            
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            logger.error("codef API 서버 오류: status={}, message={}", e.getStatusCode(), e.getMessage());
            throw new CodefTokenException("codef API 서버 오류: " + e.getStatusCode(), e);
        } catch (Exception e) {
            logger.error("토큰 발급 중 예상치 못한 오류 발생", e);
            throw new CodefTokenException("codef 토큰 발급 실패", e);
        }
    }
    
    /**
     * 토큰 강제 갱신 (스케줄러에서 호출)
     */
    @Transactional
    public void renewToken() {
        logger.info("토큰 강제 갱신 시작");
        issueNewToken();
    }
    
    /**
     * 만료 예정 토큰 확인 및 갱신
     */
    @Transactional
    public void checkAndRenewExpiringTokens() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayLater = now.plusDays(1);
        
        var expiringTokens = codefAuthRepository.findTokensExpiringWithinOneDay(now, oneDayLater);
        
        if (!expiringTokens.isEmpty()) {
            logger.info("만료 예정 토큰 {}개 발견, 갱신 진행", expiringTokens.size());
            renewToken();
        } else {
            logger.debug("만료 예정 토큰 없음");
        }
    }
    
    /**
     * 만료된 토큰들 정리
     */
    @Transactional
    public void cleanupExpiredTokens() {
        var expiredTokens = codefAuthRepository.findExpiredTokens(LocalDateTime.now());
        
        if (!expiredTokens.isEmpty()) {
            logger.info("만료된 토큰 {}개 정리", expiredTokens.size());
            expiredTokens.forEach(CodefAuth::deactivate);
            codefAuthRepository.saveAll(expiredTokens);
        }
    }
    
    /**
     * 모든 토큰 비활성화 (새 토큰 발급 전)
     */
    private void deactivateAllTokens() {
        var activeTokens = codefAuthRepository.findAll().stream()
                .filter(CodefAuth::isActive)
                .toList();
        
        activeTokens.forEach(CodefAuth::deactivate);
        codefAuthRepository.saveAll(activeTokens);
        
        logger.debug("기존 활성 토큰 {}개 비활성화", activeTokens.size());
    }
    
    /**
     * 토큰 상태 확인 (모니터링용)
     */
    public String getTokenStatus() {
        Optional<CodefAuth> validToken = codefAuthRepository.findValidToken(LocalDateTime.now());
        
        if (validToken.isPresent()) {
            CodefAuth token = validToken.get();
            return String.format("토큰 유효함 - 만료일: %s, 남은 시간: %d시간", 
                    token.getTokenExpiresAt(),
                    java.time.Duration.between(LocalDateTime.now(), token.getTokenExpiresAt()).toHours());
        } else {
            return "유효한 토큰 없음";
        }
    }
}