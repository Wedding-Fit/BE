package com.weddingfit.service.notification;

import com.weddingfit.dto.request.fcm.MessagePushServiceRequest;
import com.weddingfit.entity.goal.Goal;
import com.weddingfit.repository.fcm.FcmTokenRepository;
import com.weddingfit.repository.goal.GoalRepository;
import com.weddingfit.service.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalNotificationService {

    private final FcmService fcmService;
    private final FcmTokenRepository fcmTokenRepository;
    private final GoalRepository goalRepository;

    /**
     * Goal의 진행률을 확인하고 필요시 FCM 알림 발송
     */
    @Transactional
    public void checkAndSendProgressNotification(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다: " + goalId));

        int progressPercent = calculateProgressPercent(goal);
        log.info("Goal {} - 진행률 계산 완료: {}% (현재금액: {}, 목표금액: {})", 
                goalId, progressPercent, goal.getCurrentAmount(), goal.getTargetAmount());
        
        // 30%, 60%, 100% 달성 시점 체크 및 알림 발송
        if (progressPercent >= 30 && !goal.getNotified30()) {
            log.info("Goal {} - 30% 달성 알림 발송 시작", goalId);
            sendProgressNotification(goal, 30);
            goal.setNotified30(true);
        }
        
        if (progressPercent >= 60 && !goal.getNotified60()) {
            log.info("Goal {} - 60% 달성 알림 발송 시작", goalId);
            sendProgressNotification(goal, 60);
            goal.setNotified60(true);
        }
        
        if (progressPercent >= 100 && !goal.getNotified100()) {
            log.info("Goal {} - 100% 달성 알림 발송 시작", goalId);
            sendProgressNotification(goal, 100);
            goal.setNotified100(true);
            goal.setGoalStatus(Goal.GoalStatus.COMPLETED);
        }

        goalRepository.save(goal);
        log.info("Goal {} - 알림 체크 완료", goalId);
    }

    /**
     * 진행률 계산 (현재금액 / 목표금액 * 100)
     */
    private int calculateProgressPercent(Goal goal) {
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        
        return goal.getCurrentAmount()
                .multiply(BigDecimal.valueOf(100))
                .divide(goal.getTargetAmount(), 0, RoundingMode.FLOOR)
                .intValue();
    }

    /**
     * 특정 진행률 달성 알림 발송
     */
    private void sendProgressNotification(Goal goal, int progressPercent) {
        try {
            List<String> tokens = fcmTokenRepository.findActiveTokensByGoalId(goal.getGoalId());
            
            if (tokens.isEmpty()) {
                log.warn("Goal {} - 활성 FCM 토큰이 없습니다", goal.getGoalId());
                return;
            }

            String title = getNotificationTitle(goal.getGoalName(), progressPercent);
            String message = getNotificationMessage(progressPercent);

            // 각 토큰별로 알림 발송
            for (String token : tokens) {
                MessagePushServiceRequest request = MessagePushServiceRequest.builder()
                        .targetToken(token)
                        .title(title)
                        .body(message)
                        .build();
                
                fcmService.pushMessage(request);
                log.info("Goal {} - {}% 달성 알림 발송 완료: {}", goal.getGoalId(), progressPercent, token);
            }

        } catch (Exception e) {
            log.error("Goal {} - {}% 달성 알림 발송 실패: {}", goal.getGoalId(), progressPercent, e.getMessage(), e);
        }
    }

    /**
     * 알림 제목 생성
     */
    private String getNotificationTitle(String goalName, int progressPercent) {
        return String.format("🎉 %s 목표 %d%% 달성!", goalName, progressPercent);
    }

    /**
     * 알림 메시지 생성
     */
    private String getNotificationMessage(int progressPercent) {
        return switch (progressPercent) {
            case 30 -> "목표의 30%를 달성했어요! 🎉 계속해서 화이팅!";
            case 60 -> "목표의 60%를 달성했어요! 💪 이제 절반 이상 완료되었네요!";
            case 100 -> "축하합니다! 목표를 100% 달성했어요! 🎊 정말 대단해요!";
            default -> String.format("목표의 %d%%를 달성했어요!", progressPercent);
        };
    }
}