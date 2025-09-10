package com.weddingfit.service.analytics;

import com.weddingfit.dto.response.analytics.SpendingAnalyticsResponse;
import com.weddingfit.entity.analytics.SpendingAnalytics;
import com.weddingfit.entity.user.User;
import com.weddingfit.repository.analytics.SpendingAnalyticsRepository;
import com.weddingfit.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingAnalyticsService {
    
    private final SpendingAnalyticsRepository spendingAnalyticsRepository;
    private final UserRepository userRepository;
    private final SpendingAnalyticsWriteService writeService;
    
    @Transactional(readOnly = true)
    public SpendingAnalyticsResponse getSpendingAnalytics(Long userId) {
        log.info("소비 패턴 분석 조회 요청: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        // 1. 오늘 날짜 기준으로 저장된 분석 데이터 조회
        LocalDate today = LocalDate.now();
        SpendingAnalytics todayAnalytics = spendingAnalyticsRepository
                .findByUserAndDate(user, today)
                .orElse(null);
        
        // 2. 오늘 분석 데이터가 없거나 갱신이 필요한 경우 새로 계산
        if (todayAnalytics == null) {
            log.info("오늘 분석 데이터가 없음, 새로 계산: userId={}", userId);
            todayAnalytics = writeService.calculateAndSaveAnalytics(user, today);
        }
        
        // 3. 응답 데이터 생성
        return SpendingAnalyticsResponse.builder()
                .totalCost(todayAnalytics.getTotalCost())
                .aiMessage(todayAnalytics.getAiMessage())
                .food(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(todayAnalytics.getFoodCost())
                        .average(todayAnalytics.getFoodAverage())
                        .build())
                .culture(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(todayAnalytics.getCultureCost())
                        .average(todayAnalytics.getCultureAverage())
                        .build())
                .medical(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(todayAnalytics.getMedicalCost())
                        .average(todayAnalytics.getMedicalAverage())
                        .build())
                .transport(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(todayAnalytics.getTransportCost())
                        .average(todayAnalytics.getTransportAverage())
                        .build())
                .shopping(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(todayAnalytics.getShoppingCost())
                        .average(todayAnalytics.getShoppingAverage())
                        .build())
                .education(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(todayAnalytics.getEducationCost())
                        .average(todayAnalytics.getEducationAverage())
                        .build())
                .communication(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(todayAnalytics.getCommunicationCost())
                        .average(todayAnalytics.getCommunicationAverage())
                        .build())
                .etc(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(todayAnalytics.getEtcCost())
                        .average(todayAnalytics.getEtcAverage())
                        .build())
                .build();
    }
    
}