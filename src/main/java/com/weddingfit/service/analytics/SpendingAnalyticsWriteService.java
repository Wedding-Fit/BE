package com.weddingfit.service.analytics;

import com.weddingfit.entity.account.Account;
import com.weddingfit.entity.analytics.SpendingAnalytics;
import com.weddingfit.entity.transaction.Transaction;
import com.weddingfit.entity.user.User;
import com.weddingfit.repository.account.AccountRepository;
import com.weddingfit.repository.analytics.SpendingAnalyticsRepository;
import com.weddingfit.repository.transaction.TransactionRepository;
import com.weddingfit.service.openai.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingAnalyticsWriteService {
    
    private final SpendingAnalyticsRepository spendingAnalyticsRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final OpenAiService openAiService;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public SpendingAnalytics calculateAndSaveAnalytics(User user, LocalDate analysisDate) {
        log.info("소비 패턴 분석 계산 시작: userId={}, date={}", user.getId(), analysisDate);
        
        // 1. 사용자의 모든 계좌 조회
        List<Account> userAccounts = accountRepository.findByUser(user);
        if (userAccounts.isEmpty()) {
            throw new IllegalArgumentException("분석할 거래 데이터가 없습니다");
        }
        
        // 2. 최근 30일간의 거래 데이터 조회 (지출만)
        LocalDate startDate = analysisDate.minusDays(30);
        List<Transaction> transactions = transactionRepository
                .findByAccountInAndTransactionDateBetweenAndTransactionType(
                        userAccounts, 
                        java.sql.Date.valueOf(startDate), 
                        java.sql.Date.valueOf(analysisDate),
                        Transaction.TransactionType.EXPENSE
                );
        
        if (transactions.isEmpty()) {
            throw new IllegalArgumentException("분석할 거래 데이터가 없습니다");
        }
        
        // 3. 카테고리별 지출 계산
        Map<Transaction.TransactionCategory, Long> categorySpending = calculateCategorySpending(transactions);
        
        // 4. 전체 지출 총액
        long totalCost = categorySpending.values().stream().mapToLong(Long::longValue).sum();
        
        // 5. 평균 계산 (임시로 현재 지출의 50%로 설정, 나중에 실제 평균 로직 추가)
        Map<Transaction.TransactionCategory, Long> categoryAverages = calculateCategoryAverages(categorySpending);
        
        // 6. 가장 많이 지출한 카테고리 찾기
        Transaction.TransactionCategory maxSpendingCategory = categorySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Transaction.TransactionCategory.ETC);
        
        // 7. AI 메시지 생성 (OpenAI API 사용)
        String aiMessage = openAiService.generateSpendingAdvice(categorySpending, categoryAverages, totalCost);
        
        // 8. 분석 데이터 저장
        SpendingAnalytics analytics = SpendingAnalytics.builder()
                .user(user)
                .analysisDate(analysisDate)
                .category(maxSpendingCategory.name()) // 가장 많이 지출한 카테고리
                .totalCost(totalCost)
                .aiMessage(aiMessage)
                .foodCost(categorySpending.getOrDefault(Transaction.TransactionCategory.FOOD, 0L))
                .foodAverage(categoryAverages.getOrDefault(Transaction.TransactionCategory.FOOD, 0L))
                .cultureCost(categorySpending.getOrDefault(Transaction.TransactionCategory.CULTURE, 0L))
                .cultureAverage(categoryAverages.getOrDefault(Transaction.TransactionCategory.CULTURE, 0L))
                .medicalCost(categorySpending.getOrDefault(Transaction.TransactionCategory.MEDICAL, 0L))
                .medicalAverage(categoryAverages.getOrDefault(Transaction.TransactionCategory.MEDICAL, 0L))
                .transportCost(categorySpending.getOrDefault(Transaction.TransactionCategory.TRANSPORT, 0L))
                .transportAverage(categoryAverages.getOrDefault(Transaction.TransactionCategory.TRANSPORT, 0L))
                .shoppingCost(categorySpending.getOrDefault(Transaction.TransactionCategory.SHOPPING, 0L))
                .shoppingAverage(categoryAverages.getOrDefault(Transaction.TransactionCategory.SHOPPING, 0L))
                .educationCost(categorySpending.getOrDefault(Transaction.TransactionCategory.EDU, 0L))
                .educationAverage(categoryAverages.getOrDefault(Transaction.TransactionCategory.EDU, 0L))
                .communicationCost(categorySpending.getOrDefault(Transaction.TransactionCategory.TELECOM, 0L))
                .communicationAverage(categoryAverages.getOrDefault(Transaction.TransactionCategory.TELECOM, 0L))
                .etcCost(categorySpending.getOrDefault(Transaction.TransactionCategory.ETC, 0L))
                .etcAverage(categoryAverages.getOrDefault(Transaction.TransactionCategory.ETC, 0L))
                .build();
        
        // 분석 결과 저장
        SpendingAnalytics savedAnalytics = spendingAnalyticsRepository.save(analytics);
        log.info("소비 패턴 분석 계산 및 저장 완료: userId={}", user.getId());
        
        return savedAnalytics;
    }
    
    private Map<Transaction.TransactionCategory, Long> calculateCategorySpending(List<Transaction> transactions) {
        Map<Transaction.TransactionCategory, Long> categorySpending = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            Transaction.TransactionCategory category = transaction.getCategory();
            if (category == null) {
                category = Transaction.TransactionCategory.ETC;
            }
            
            long amount = transaction.getAmount() != null ? 
                    transaction.getAmount().longValue() : 0L;
            
            categorySpending.merge(category, Math.abs(amount), Long::sum);
        }
        
        return categorySpending;
    }
    
    private Map<Transaction.TransactionCategory, Long> calculateCategoryAverages(
            Map<Transaction.TransactionCategory, Long> categorySpending) {
        Map<Transaction.TransactionCategory, Long> categoryAverages = new HashMap<>();
        
        // 임시로 현재 지출의 50%를 평균으로 설정 (나중에 실제 평균 로직 구현)
        for (Map.Entry<Transaction.TransactionCategory, Long> entry : categorySpending.entrySet()) {
            categoryAverages.put(entry.getKey(), entry.getValue() / 2);
        }
        
        return categoryAverages;
    }
    
}