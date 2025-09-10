package com.weddingfit.service.goal;

import com.weddingfit.dto.request.goal.GoalCurrentAmountUpdateRequest;
import com.weddingfit.entity.goal.Goal;
import com.weddingfit.repository.goal.GoalRepository;
import com.weddingfit.service.notification.GoalNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalNotificationService goalNotificationService;

    @InjectMocks
    private GoalServiceImpl goalService;

    private Goal testGoal;
    private GoalCurrentAmountUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testGoal = Goal.builder()
                .goalId(1L)
                .coupleId(1L)
                .goalName("신혼여행 적금")
                .targetAmount(new BigDecimal("10000000"))
                .currentAmount(new BigDecimal("2000000"))
                .targetDate(LocalDate.now().plusMonths(12))
                .notified30(false)
                .notified60(false)
                .notified100(false)
                .goalStatus(Goal.GoalStatus.ACTIVE)
                .build();

        updateRequest = GoalCurrentAmountUpdateRequest.builder()
                .currentAmount(new BigDecimal("3000000"))
                .reason("적금 입금 완료")
                .build();
    }

    @Test
    @DisplayName("목표 현재 금액 업데이트 성공 테스트")
    void testUpdateCurrentAmountSuccess() {
        // given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);

        // when
        goalService.updateCurrentAmount(1L, updateRequest);

        // then
        assertEquals(new BigDecimal("3000000"), testGoal.getCurrentAmount());
        verify(goalRepository).save(testGoal);
        verify(goalNotificationService).checkAndSendProgressNotification(1L);
    }

    @Test
    @DisplayName("존재하지 않는 목표 ID로 업데이트 시 예외 발생")
    void testUpdateCurrentAmountWhenGoalNotFound() {
        // given
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            goalService.updateCurrentAmount(999L, updateRequest);
        });

        verify(goalRepository, never()).save(any());
        verify(goalNotificationService, never()).checkAndSendProgressNotification(any());
    }

    @Test
    @DisplayName("목표 금액을 0으로 업데이트")
    void testUpdateCurrentAmountToZero() {
        // given
        updateRequest = GoalCurrentAmountUpdateRequest.builder()
                .currentAmount(BigDecimal.ZERO)
                .reason("초기화")
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);

        // when
        goalService.updateCurrentAmount(1L, updateRequest);

        // then
        assertEquals(BigDecimal.ZERO, testGoal.getCurrentAmount());
        verify(goalRepository).save(testGoal);
        verify(goalNotificationService).checkAndSendProgressNotification(1L);
    }

    @Test
    @DisplayName("목표 금액을 목표액보다 크게 업데이트 (100% 초과)")
    void testUpdateCurrentAmountExceedingTarget() {
        // given
        updateRequest = GoalCurrentAmountUpdateRequest.builder()
                .currentAmount(new BigDecimal("15000000")) // 목표액 10,000,000 초과
                .reason("목표 초과 달성")
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);

        // when
        goalService.updateCurrentAmount(1L, updateRequest);

        // then
        assertEquals(new BigDecimal("15000000"), testGoal.getCurrentAmount());
        verify(goalRepository).save(testGoal);
        verify(goalNotificationService).checkAndSendProgressNotification(1L);
    }

    @Test
    @DisplayName("알림 서비스 호출 실패 시에도 금액 업데이트는 완료")
    void testUpdateCurrentAmountWhenNotificationFails() {
        // given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);
        doThrow(new RuntimeException("알림 발송 실패"))
                .when(goalNotificationService).checkAndSendProgressNotification(1L);

        // when & then
        assertThrows(RuntimeException.class, () -> {
            goalService.updateCurrentAmount(1L, updateRequest);
        });

        // 금액 업데이트는 알림 서비스 호출 전에 완료되어야 함
        assertEquals(new BigDecimal("3000000"), testGoal.getCurrentAmount());
        verify(goalRepository).save(testGoal);
    }

    @Test
    @DisplayName("연속된 금액 업데이트 시 각각 알림 서비스 호출")
    void testMultipleCurrentAmountUpdates() {
        // given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);

        GoalCurrentAmountUpdateRequest firstUpdate = GoalCurrentAmountUpdateRequest.builder()
                .currentAmount(new BigDecimal("3000000"))
                .build();

        GoalCurrentAmountUpdateRequest secondUpdate = GoalCurrentAmountUpdateRequest.builder()
                .currentAmount(new BigDecimal("6000000"))
                .build();

        // when
        goalService.updateCurrentAmount(1L, firstUpdate);
        goalService.updateCurrentAmount(1L, secondUpdate);

        // then
        assertEquals(new BigDecimal("6000000"), testGoal.getCurrentAmount());
        verify(goalRepository, times(2)).save(testGoal);
        verify(goalNotificationService, times(2)).checkAndSendProgressNotification(1L);
    }
}