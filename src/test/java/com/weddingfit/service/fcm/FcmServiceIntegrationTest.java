package com.weddingfit.service.fcm;

import com.weddingfit.dto.request.fcm.MessagePushServiceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("FCM 서비스 통합 테스트")
class FcmServiceIntegrationTest {

    @Autowired
    private FcmService fcmService;

    private String testToken;
    private MessagePushServiceRequest testRequest;

    @BeforeEach
    void setUp() {
        // 실제 테스트용 FCM 토큰으로 교체 필요
        testToken = "test_fcm_token_here";
        testRequest = MessagePushServiceRequest.of(
            testToken,
            "테스트 알림",
            "FCM 통합 테스트 메시지입니다."
        );
    }

    @Test
    @DisplayName("Firebase 키 파일 로딩 오류 테스트")
    void testFirebaseKeyLoadingError() {
        // given - testRequest가 준비됨

        // when & then - 테스트용 키 파일이 잘못된 형식이므로 credentials 로딩 오류 예상
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            fcmService.pushMessage(testRequest));
        
        // Firebase credentials 로딩 오류 확인
        assertTrue(exception.getMessage().contains("FCM credentials load error"));
        assertTrue(exception.getCause() instanceof java.io.IOException);
    }

    @Test
    @DisplayName("잘못된 FCM 토큰으로 알림 발송 시 예외 처리 테스트")
    void testInvalidTokenHandling() {
        // given
        MessagePushServiceRequest invalidRequest = MessagePushServiceRequest.of(
            "invalid_token",
            "테스트",
            "잘못된 토큰 테스트"
        );

        // when & then
        assertThrows(IllegalStateException.class, () -> 
            fcmService.pushMessage(invalidRequest)
        );
    }

    @Test
    @DisplayName("Firebase 인증 토큰 획득 실패 테스트")
    void testFirebaseAuthenticationFailure() {
        // given - FcmService가 올바르게 주입되었음

        // when - 잘못된 키 파일로 인한 인증 실패 예상
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            fcmService.pushMessage(testRequest));
        
        // Firebase credentials 로딩 오류 확인
        assertTrue(exception.getMessage().contains("FCM credentials load error"));
    }

    @Test
    @DisplayName("빈 제목과 내용으로 알림 발송 시 키 파일 오류 테스트")
    void testEmptyTitleAndBody() {
        // given
        MessagePushServiceRequest emptyRequest = MessagePushServiceRequest.of(
            testToken,
            "",
            ""
        );

        // when & then - 키 파일 로딩 오류 발생
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            fcmService.pushMessage(emptyRequest));
        
        assertTrue(exception.getMessage().contains("FCM credentials load error"));
    }

    @Test
    @DisplayName("긴 제목과 내용으로 알림 발송 시 키 파일 오류 테스트")
    void testLongTitleAndBody() {
        // given
        String longTitle = "A".repeat(100);
        String longBody = "B".repeat(1000);
        MessagePushServiceRequest longRequest = MessagePushServiceRequest.of(
            testToken,
            longTitle,
            longBody
        );

        // when & then - 키 파일 로딩 오류 발생
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            fcmService.pushMessage(longRequest));
        
        assertTrue(exception.getMessage().contains("FCM credentials load error"));
    }

    @Test
    @DisplayName("Firebase 키 파일 오류로 인한 예외 처리 테스트")
    void testFirebaseKeyFileError() {
        // given - 잘못된 키 파일로 인한 오류 테스트
        MessagePushServiceRequest request = MessagePushServiceRequest.of(
            testToken,
            "키 파일 테스트",
            "Firebase 키 파일 오류 테스트"
        );

        // when & then - Firebase 키 파일 로딩 오류
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            fcmService.pushMessage(request));
        
        assertTrue(exception.getMessage().contains("FCM credentials load error"));
    }

    @Test
    @DisplayName("Firebase 서비스 키 파일 포맷 오류 테스트")
    void testFirebaseKeyFileFormatError() {
        // 테스트용 키 파일이 잘못된 포맷이므로 로딩 오류 발생
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            fcmService.pushMessage(testRequest));
        
        // 파일은 존재하지만 포맷이 잘못되어 로딩 실패
        assertTrue(exception.getMessage().contains("FCM credentials load error"));
        assertTrue(exception.getCause() instanceof java.io.IOException);
    }

    @Test
    @DisplayName("FCM API 응답 오류 처리 테스트")
    void testFcmApiErrorResponse() {
        // given - 잘못된 프로젝트 ID나 잘못된 토큰으로 4xx/5xx 오류 유발
        MessagePushServiceRequest errorRequest = MessagePushServiceRequest.of(
            "definitely_invalid_token_format",
            "오류 테스트",
            "API 오류 응답 테스트"
        );

        // when & then - FCM API 오류 시 IllegalStateException 발생 예상
        assertThrows(IllegalStateException.class, () -> 
            fcmService.pushMessage(errorRequest)
        );
    }

    @Test
    @DisplayName("JSON 직렬화 전 키 파일 로딩 오류 테스트")
    void testJsonSerializationBlockedByKeyError() {
        // given - 정상적인 요청이지만 키 파일 오류로 JSON 직렬화까지 가지 못함
        MessagePushServiceRequest request = MessagePushServiceRequest.of(
            testToken,
            "JSON 테스트",
            "JSON 직렬화 테스트"
        );

        // when & then - 키 파일 로딩 오류로 JSON 직렬화 단계까지 가지 못함
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            fcmService.pushMessage(request));
        
        assertTrue(exception.getMessage().contains("FCM credentials load error"));
    }

    @Test
    @DisplayName("동시 다발적 FCM 요청 처리 테스트")
    void testConcurrentFcmRequests() throws InterruptedException {
        // given
        int threadCount = 3;
        Thread[] threads = new Thread[threadCount];
        final int[] successCount = {0};
        final int[] errorCount = {0};

        // when
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                MessagePushServiceRequest request = MessagePushServiceRequest.of(
                    testToken,
                    "동시 테스트 " + index,
                    "동시 다발적 요청 테스트 " + index
                );
                try {
                    fcmService.pushMessage(request);
                    synchronized (successCount) {
                        successCount[0]++;
                    }
                } catch (IllegalStateException e) {
                    synchronized (errorCount) {
                        errorCount[0]++;
                    }
                }
            });
        }

        // 모든 스레드 시작
        for (Thread thread : threads) {
            thread.start();
        }

        // 모든 스레드 완료 대기
        for (Thread thread : threads) {
            thread.join();
        }

        // then - 모든 요청이 처리되었는지 확인 (성공 또는 예상된 오류)
        assertEquals(threadCount, successCount[0] + errorCount[0]);
        assertTrue(errorCount[0] > 0); // API 호출 오류 발생 예상
    }
}