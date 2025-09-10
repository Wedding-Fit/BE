package com.weddingfit.service.fcm;

import com.weddingfit.dto.request.fcm.MessagePushServiceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("FCM 서비스 간단한 테스트")
class FcmServiceSimpleTest {

    @Autowired
    private FcmService fcmService;

    private MessagePushServiceRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = MessagePushServiceRequest.of(
            "test_fcm_token_here",
            "테스트 알림",
            "FCM 간단한 테스트 메시지입니다."
        );
    }

    @Test
    @DisplayName("FCM 서비스 동작 확인 - 예외 종류 파악")
    void testFcmServiceBehavior() {
        try {
            fcmService.pushMessage(testRequest);
            System.out.println("✅ FCM 메시지 전송 성공!");
        } catch (Exception e) {
            System.out.println("❌ 예외 발생: " + e.getClass().getName());
            System.out.println("❌ 예외 메시지: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("❌ 원인: " + e.getCause().getClass().getName());
                System.out.println("❌ 원인 메시지: " + e.getCause().getMessage());
            }
        }
    }
}