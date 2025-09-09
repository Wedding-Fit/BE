package com.weddingfit.service.codef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "codef.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class CodefTokenScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(CodefTokenScheduler.class);
    
    private final CodefAuthService codefAuthService;
    
    @Autowired
    public CodefTokenScheduler(CodefAuthService codefAuthService) {
        this.codefAuthService = codefAuthService;
    }
    
    /**
     * 매 시간마다 토큰 만료 여부 확인 및 갱신
     */
    @Scheduled(fixedRate = 3600000) // 1시간 = 3,600,000ms
    public void checkTokenExpiration() {
        logger.debug("토큰 만료 여부 확인 시작");
        try {
            codefAuthService.checkAndRenewExpiringTokens();
        } catch (Exception e) {
            logger.error("토큰 만료 확인 중 오류 발생", e);
        }
    }
    
    /**
     * 매일 새벽 3시에 만료된 토큰 정리
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredTokens() {
        logger.info("만료된 토큰 정리 작업 시작");
        try {
            codefAuthService.cleanupExpiredTokens();
        } catch (Exception e) {
            logger.error("토큰 정리 중 오류 발생", e);
        }
    }
    
    /**
     * 매일 오전 9시에 토큰 상태 로깅 (모니터링용)
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void logTokenStatus() {
        logger.info("현재 토큰 상태: {}", codefAuthService.getTokenStatus());
    }
}