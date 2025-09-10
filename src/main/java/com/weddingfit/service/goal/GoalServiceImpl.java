package com.weddingfit.service.goal;

import com.weddingfit.dto.request.goal.GoalCreateRequest;
import com.weddingfit.dto.request.goal.GoalCurrentAmountUpdateRequest;
import com.weddingfit.dto.request.goal.GoalSaveProductRequest;
import com.weddingfit.dto.response.goal.*;
import com.weddingfit.entity.goal.Goal;
import com.weddingfit.entity.deposit.DepositSaving;
import com.weddingfit.repository.goal.GoalRepository;
import com.weddingfit.repository.deposit.DepositSavingRepository;
import com.weddingfit.repository.company.FinancialCompanyRepository;
import com.weddingfit.service.notification.GoalNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final DepositSavingRepository depositSavingRepository;
    private final FinancialCompanyRepository financialCompanyRepository;
    private final GoalNotificationService goalNotificationService;

    @Override
    @Transactional
    public GoalCreatedResponse createGoal(GoalCreateRequest request) {
        // 1. 목표 저장
        Goal goal = Goal.builder()
                .coupleId(request.getCoupleId())
                .category(request.getCategory())
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .investmentStyle(request.getInvestmentStyle())
                .currentAmount(BigDecimal.ZERO)
                .goalStatus(Goal.GoalStatus.ACTIVE)
                .build();

        Goal saved = goalRepository.save(goal);

        // 2. 남은 개월 수 계산
        int monthsLeft = (int) ChronoUnit.MONTHS.between(LocalDate.now(), goal.getTargetDate());
        if (monthsLeft <= 0) monthsLeft = 1;
        final int finalMonthsLeft = monthsLeft;

        // 3. 단리 기준 월 납입액 계산 함수 정의
        Function<DepositSaving, BigDecimal> calculateMonthlyPayment = product -> {
            BigDecimal rate = product.getInterestRate().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            BigDecimal totalRate = BigDecimal.ONE.add(rate.multiply(BigDecimal.valueOf(finalMonthsLeft)));
            return goal.getTargetAmount()
                    .divide(totalRate, 10, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(finalMonthsLeft), 10, RoundingMode.HALF_UP);
        };

        // 4. 예적금 상품 통합 조회 (MIXED 대응)
        List<DepositSaving> productList = goal.getInvestmentStyle().toDepositSavingTypes().stream()
                .flatMap(type -> depositSavingRepository
                        .findTop10ByTypeOrderByInterestRateDesc(type)
                        .stream())
                .collect(Collectors.toList());

        // 5. 조건에 맞는 추천 상품 필터링 및 계산
        List<ProductResponse> recommendedProducts = productList.stream()
                .filter(p -> {
                    try {
                        int saveMonth = Integer.parseInt(p.getSaveMonth());
                        return saveMonth <= finalMonthsLeft;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .limit(5)
                .map(p -> {
                    BigDecimal monthlyPayment = calculateMonthlyPayment.apply(p);
                    BigDecimal totalRate = BigDecimal.ONE.add(
                            p.getInterestRate().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(finalMonthsLeft))
                    );
                    BigDecimal estimatedAmount = monthlyPayment
                            .multiply(BigDecimal.valueOf(finalMonthsLeft))
                            .multiply(totalRate);

                    return ProductResponse.builder()
                            .productId(p.getId())
                            .companyName(p.getCompany().getName())
                            .productName(p.getName())
                            .type(p.getType())
                            .interestRate(p.getInterestRate())
                            .monthlyPayment(monthlyPayment.setScale(0, RoundingMode.DOWN))
                            .estimatedMaturityDate(goal.getTargetDate())
                            .estimatedAmount(estimatedAmount.setScale(0, RoundingMode.DOWN))
                            .build();
                })
                .collect(Collectors.toList());

        // 6. 예외 처리: 추천 불가
        if (recommendedProducts.isEmpty()) {
            throw new IllegalArgumentException("달성 가능한 상품이 없습니다. 목표를 다시 설정해주세요.");
        }

        // 7. 목표 요약 정보 구성
        GoalCreatedResponse.GoalInfo info = GoalCreatedResponse.GoalInfo.builder()
                .goalId(saved.getGoalId())
                .category(saved.getCategory())
                .goalName(saved.getGoalName())
                .targetAmount(saved.getTargetAmount())
                .currentAmount(saved.getCurrentAmount())
                .targetDate(saved.getTargetDate())
                .investmentStyle(saved.getInvestmentStyle())
                .progressPercent(0)
                .monthsLeft(finalMonthsLeft)
                .monthlyNeeded(
                        saved.getTargetAmount().divide(BigDecimal.valueOf(finalMonthsLeft), 0, RoundingMode.DOWN)
                )
                .build();

        // 8. 응답 반환
        return GoalCreatedResponse.builder()
                .goal(info)
                .products(recommendedProducts)
                .build();
    }

    @Override
    public List<GoalListResponse> getGoalsByCoupleId(Long coupleId) {
        List<Goal> goals = goalRepository.findByCoupleId(coupleId);

        return goals.stream().map(goal -> {
            int totalMonth = (int) ChronoUnit.MONTHS.between(goal.getCreatedAt().toLocalDate(), goal.getTargetDate());
            int progress = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                    ? goal.getCurrentAmount().multiply(BigDecimal.valueOf(100)).divide(goal.getTargetAmount(), 0, RoundingMode.FLOOR).intValue()
                    : 0;
            return GoalListResponse.builder()
                    .goalId(goal.getGoalId())
                    .category(goal.getCategory())
                    .goalName(goal.getGoalName())
                    .targetAmount(goal.getTargetAmount())
                    .currentAmount(goal.getCurrentAmount())
                    .progressPercent(progress)
                    .startDate(goal.getCreatedAt().toLocalDate())
                    .totalMonth(totalMonth)
                    .goalStatus(goal.getGoalStatus())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public GoalDetailResponse getGoalDetail(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("해당 목표를 찾을 수 없습니다."));

        int totalMonth = (int) ChronoUnit.MONTHS.between(goal.getCreatedAt().toLocalDate(), goal.getTargetDate());
        int remainingMonths = (int) ChronoUnit.MONTHS.between(LocalDate.now(), goal.getTargetDate());
        if (remainingMonths < 0) remainingMonths = 0;

        int progress = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                ? goal.getCurrentAmount().multiply(BigDecimal.valueOf(100))
                .divide(goal.getTargetAmount(), 0, RoundingMode.FLOOR).intValue()
                : 0;

        BigDecimal monthlyTarget = goal.getTargetAmount()
                .divide(BigDecimal.valueOf(totalMonth), RoundingMode.UP);

        ProductResponse product = null;
        if (goal.getFinancialProductId() != null) {
            Optional<DepositSaving> saved = depositSavingRepository.findById(goal.getFinancialProductId());
            if (saved.isPresent()) {
                DepositSaving p = saved.get();

                int monthsLeft = Math.max((int) ChronoUnit.MONTHS.between(LocalDate.now(), goal.getTargetDate()), 1);
                BigDecimal rate = p.getInterestRate().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                BigDecimal totalRate = BigDecimal.ONE.add(rate.multiply(BigDecimal.valueOf(monthsLeft)));
                BigDecimal monthlyPayment = goal.getTargetAmount()
                        .divide(totalRate, 10, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(monthsLeft), 10, RoundingMode.HALF_UP);

                BigDecimal estimatedAmount = monthlyPayment
                        .multiply(BigDecimal.valueOf(monthsLeft))
                        .multiply(totalRate);

                product = ProductResponse.builder()
                        .productId(p.getId())
                        .companyName(p.getCompany().getName())
                        .productName(p.getName())
                        .type(p.getType())
                        .interestRate(p.getInterestRate())
                        .monthlyPayment(monthlyPayment.setScale(0, RoundingMode.DOWN)) // ✅ 추가
                        .estimatedMaturityDate(goal.getTargetDate())
                        .estimatedAmount(estimatedAmount.setScale(0, RoundingMode.DOWN)) // ✅ 추가
                        .build();
            }
        }

        return GoalDetailResponse.builder()
                .goalId(goal.getGoalId())
                .coupleId(goal.getCoupleId())
                .category(goal.getCategory())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .monthlyTarget(monthlyTarget)
                .investmentStyle(goal.getInvestmentStyle())
                .goalStatus(goal.getGoalStatus())
                .progressPercent(progress)
                .totalMonth(totalMonth)
                .remainingMonths(remainingMonths)
                .product(product)
                .build();
    }

    @Override
    @Transactional
    public void saveGoalProduct(Long goalId, GoalSaveProductRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("해당 목표를 찾을 수 없습니다."));

        goal.setFinancialProductId(request.getProductId());
        goal.setMonthlyPayment(request.getMonthlyPayment());       // ⬅️ 월 납입액 저장
        goal.setEstimatedAmount(request.getEstimatedAmount());     // ⬅️ 예상 달성금 저장

        goalRepository.save(goal);
    }

    @Override
    @Transactional
    public void updateCurrentAmount(Long goalId, GoalCurrentAmountUpdateRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("해당 목표를 찾을 수 없습니다."));

        goal.setCurrentAmount(request.getCurrentAmount());
        goalRepository.save(goal);

        // FCM 알림 체크 및 발송
        goalNotificationService.checkAndSendProgressNotification(goalId);
    }

    @Override
    @Transactional
    public void deleteGoal(Long goalId) {
        goalRepository.deleteById(goalId);
    }
}