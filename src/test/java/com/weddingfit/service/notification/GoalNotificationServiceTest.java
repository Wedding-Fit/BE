package com.weddingfit.service.notification;

import com.weddingfit.dto.request.fcm.MessagePushServiceRequest;
import com.weddingfit.entity.goal.Goal;
import com.weddingfit.repository.fcm.FcmTokenRepository;
import com.weddingfit.repository.goal.GoalRepository;
import com.weddingfit.service.fcm.FcmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalNotificationServiceTest {

    @Mock
    private FcmService fcmService;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private GoalNotificationService goalNotificationService;

    private Goal testGoal;
    private List<String> testTokens;

    @BeforeEach
    void setUp() {
        testGoal = Goal.builder()
                .goalId(1L)
                .coupleId(1L)
                .goalName("신혼여행 적금")
                .targetAmount(new BigDecimal("10000000"))
                .currentAmount(new BigDecimal("3000000"))
                .targetDate(LocalDate.now().plusMonths(12))
                .notified30(false)
                .notified60(false)
                .notified100(false)
                .goalStatus(Goal.GoalStatus.ACTIVE)
                .build();

        testTokens = Arrays.asList("token1", "token2");
    }

    @Test
    @DisplayName("30% 달성 시 알림 발송 테스트")
    void testSend30PercentNotification() {
        // given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(fcmTokenRepository.findActiveTokensByGoalId(1L)).thenReturn(testTokens);

        // when
        goalNotificationService.checkAndSendProgressNotification(1L);

        // then
        verify(fcmService, times(2)).pushMessage(any(MessagePushServiceRequest.class));
        verify(goalRepository).save(testGoal);
        assertTrue(testGoal.getNotified30());
        assertFalse(testGoal.getNotified60());
        assertFalse(testGoal.getNotified100());
        assertEquals(Goal.GoalStatus.ACTIVE, testGoal.getGoalStatus());
    }

    @Test
    @DisplayName("60% 달성 시 알림 발송 테스트")
    void testSend60PercentNotification() {
        // given
        testGoal.setCurrentAmount(new BigDecimal("6000000")); // 60% 달성
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(fcmTokenRepository.findActiveTokensByGoalId(1L)).thenReturn(testTokens);

        // when
        goalNotificationService.checkAndSendProgressNotification(1L);

        // then
        verify(fcmService, times(4)).pushMessage(any(MessagePushServiceRequest.class)); // 30% + 60%
        assertTrue(testGoal.getNotified30());
        assertTrue(testGoal.getNotified60());
        assertFalse(testGoal.getNotified100());
    }

    @Test
    @DisplayName("100% 달성 시 알림 발송 및 목표 완료 처리 테스트")
    void testSend100PercentNotificationAndCompleteGoal() {
        // given
        testGoal.setCurrentAmount(new BigDecimal("10000000")); // 100% 달성
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(fcmTokenRepository.findActiveTokensByGoalId(1L)).thenReturn(testTokens);

        // when
        goalNotificationService.checkAndSendProgressNotification(1L);

        // then
        verify(fcmService, times(6)).pushMessage(any(MessagePushServiceRequest.class)); // 30% + 60% + 100%
        assertTrue(testGoal.getNotified30());
        assertTrue(testGoal.getNotified60());
        assertTrue(testGoal.getNotified100());
        assertEquals(Goal.GoalStatus.COMPLETED, testGoal.getGoalStatus());
    }

    @Test
    @DisplayName("이미 알림 발송된 경우 중복 발송 방지 테스트")
    void testPreventDuplicateNotification() {
        // given
        testGoal.setNotified30(true); // 이미 30% 알림 발송됨
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));

        // when
        goalNotificationService.checkAndSendProgressNotification(1L);

        // then
        verify(fcmService, never()).pushMessage(any(MessagePushServiceRequest.class));
        verify(goalRepository).save(testGoal);
    }

    @Test
    @DisplayName("FCM 토큰이 없는 경우 알림 발송 생략 테스트")
    void testSkipNotificationWhenNoTokens() {
        // given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(fcmTokenRepository.findActiveTokensByGoalId(1L)).thenReturn(Arrays.asList());

        // when
        goalNotificationService.checkAndSendProgressNotification(1L);

        // then
        verify(fcmService, never()).pushMessage(any(MessagePushServiceRequest.class));
        assertTrue(testGoal.getNotified30()); // 플래그는 설정됨 (중복 방지용)
    }

    @Test
    @DisplayName("FCM 발송 실패 시 예외 처리 테스트")
    void testHandleFcmSendFailure() {
        // given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(fcmTokenRepository.findActiveTokensByGoalId(1L)).thenReturn(testTokens);
        doThrow(new RuntimeException("FCM 발송 실패")).when(fcmService).pushMessage(any());

        // when & then
        assertDoesNotThrow(() -> goalNotificationService.checkAndSendProgressNotification(1L));
        verify(goalRepository).save(testGoal); // 플래그 업데이트는 여전히 실행됨
    }

    @Test
    @DisplayName("존재하지 않는 목표 ID로 호출 시 예외 발생 테스트")
    void testThrowExceptionWhenGoalNotFound() {
        // given
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            goalNotificationService.checkAndSendProgressNotification(999L);
        });
    }

    @Test
    @DisplayName("진행률 0% 시 알림 발송하지 않음 테스트")
    void testNoNotificationWhenZeroProgress() {
        // given
        testGoal.setCurrentAmount(BigDecimal.ZERO);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));

        // when
        goalNotificationService.checkAndSendProgressNotification(1L);

        // then
        verify(fcmService, never()).pushMessage(any(MessagePushServiceRequest.class));
        assertFalse(testGoal.getNotified30());
        assertFalse(testGoal.getNotified60());
        assertFalse(testGoal.getNotified100());
    }
}