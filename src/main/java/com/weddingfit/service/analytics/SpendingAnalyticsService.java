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
import java.util.List;

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
        List<SpendingAnalytics> todayAnalyticsList = spendingAnalyticsRepository
                .findByUserAndDate(user, today);
        
        // 2. 오늘 분석 데이터가 없거나 갱신이 필요한 경우 새로 계산
        if (todayAnalyticsList.isEmpty()) {
            log.info("오늘 분석 데이터가 없음, 새로 계산: userId={}", userId);
            SpendingAnalytics todayAnalytics = writeService.calculateAndSaveAnalytics(user, today);
            todayAnalyticsList = List.of(todayAnalytics);
        }
        
        // 3. 카테고리별 합계 계산
        long totalCost = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getTotalCost).sum();
        long foodCost = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getFoodCost).sum();
        long foodAverage = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getFoodAverage).findFirst().orElse(0);
        long cultureCost = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getCultureCost).sum();
        long cultureAverage = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getCultureAverage).findFirst().orElse(0);
        long medicalCost = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getMedicalCost).sum();
        long medicalAverage = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getMedicalAverage).findFirst().orElse(0);
        long transportCost = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getTransportCost).sum();
        long transportAverage = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getTransportAverage).findFirst().orElse(0);
        long shoppingCost = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getShoppingCost).sum();
        long shoppingAverage = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getShoppingAverage).findFirst().orElse(0);
        long educationCost = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getEducationCost).sum();
        long educationAverage = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getEducationAverage).findFirst().orElse(0);
        long communicationCost = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getCommunicationCost).sum();
        long communicationAverage = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getCommunicationAverage).findFirst().orElse(0);
        long etcCost = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getEtcCost).sum();
        long etcAverage = todayAnalyticsList.stream().mapToLong(SpendingAnalytics::getEtcAverage).findFirst().orElse(0);
        
        String aiMessage = todayAnalyticsList.stream()
                .map(SpendingAnalytics::getAiMessage)
                .filter(msg -> msg != null && !msg.isEmpty())
                .findFirst()
                .orElse("");
        
        // 4. 응답 데이터 생성
        return SpendingAnalyticsResponse.builder()
                .totalCost(totalCost)
                .aiMessage(aiMessage)
                .food(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(foodCost)
                        .average(foodAverage)
                        .build())
                .culture(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(cultureCost)
                        .average(cultureAverage)
                        .build())
                .medical(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(medicalCost)
                        .average(medicalAverage)
                        .build())
                .transport(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(transportCost)
                        .average(transportAverage)
                        .build())
                .shopping(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(shoppingCost)
                        .average(shoppingAverage)
                        .build())
                .education(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(educationCost)
                        .average(educationAverage)
                        .build())
                .communication(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(communicationCost)
                        .average(communicationAverage)
                        .build())
                .etc(SpendingAnalyticsResponse.CategorySpending.builder()
                        .cost(etcCost)
                        .average(etcAverage)
                        .build())
                .build();
    }
    
}