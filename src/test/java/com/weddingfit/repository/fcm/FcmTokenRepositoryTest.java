package com.weddingfit.repository.fcm;

import com.weddingfit.entity.fcm.FcmToken;
import com.weddingfit.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FCM 토큰 레포지토리 테스트")
class FcmTokenRepositoryTest {

    @Autowired
    private FcmTokenRepository fcmTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser1;
    private User testUser2;
    private FcmToken testToken1;
    private FcmToken testToken2;
    private FcmToken expiredToken;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser1 = User.builder()
                .nickname("testUser1")
                .build();
        testUser1 = entityManager.persistAndFlush(testUser1);

        testUser2 = User.builder()
                .nickname("testUser2")
                .build();
        testUser2 = entityManager.persistAndFlush(testUser2);

        testToken1 = FcmToken.builder()
                .user(testUser1)
                .fcmToken("test_token_1")
                .platform(FcmToken.Platform.ANDROID)
                .isActive(true)
                .build();

        testToken2 = FcmToken.builder()
                .user(testUser2)
                .fcmToken("test_token_2")
                .platform(FcmToken.Platform.IOS)
                .isActive(true)
                .build();

        expiredToken = FcmToken.builder()
                .user(testUser1)
                .fcmToken("expired_token")
                .platform(FcmToken.Platform.ANDROID)
                .isActive(false)
                .build();

        fcmTokenRepository.saveAll(List.of(testToken1, testToken2, expiredToken));
    }

    @Test
    @DisplayName("사용자 ID로 활성 토큰 조회 테스트")
    void testFindByUserIdAndIsActiveTrue() {
        // when
        List<FcmToken> activeTokens = fcmTokenRepository.findByUser_IdAndIsActiveTrue(testUser1.getId());

        // then
        assertEquals(1, activeTokens.size());
        assertEquals("test_token_1", activeTokens.get(0).getFcmToken());
        assertTrue(activeTokens.get(0).getIsActive());
    }

    @Test
    @DisplayName("목표 ID로 활성 토큰 조회 테스트 (커플 기반)")
    void testFindActiveTokensByGoalId() {
        // given - 실제 구현에서는 Goal과 Couple 엔티티를 통해 연결됨
        // 여기서는 간단히 테스트만 수행 (실제 Goal, Couple 데이터 없이는 빈 결과)

        // when
        List<String> tokens = fcmTokenRepository.findActiveTokensByGoalId(1L);

        // then
        assertNotNull(tokens);
        // Goal과 Couple 데이터가 없으므로 빈 결과 예상
        assertTrue(tokens.isEmpty());
    }

    @Test
    @DisplayName("사용자 ID와 토큰으로 FCM 토큰 엔티티 조회 테스트")
    void testFindByUserIdAndFcmToken() {
        // when
        FcmToken found = fcmTokenRepository.findByUser_IdAndFcmToken(testUser1.getId(), "test_token_1").orElse(null);

        // then
        assertNotNull(found);
        assertEquals("test_token_1", found.getFcmToken());
        assertEquals(testUser1.getId(), found.getUser().getId());
        assertTrue(found.getIsActive());
    }

    @Test
    @DisplayName("비활성 토큰은 활성 토큰 조회에서 제외되는 테스트")
    void testInactiveTokensNotReturned() {
        // when
        List<FcmToken> activeTokens = fcmTokenRepository.findByUser_IdAndIsActiveTrue(testUser1.getId());

        // then
        assertEquals(1, activeTokens.size());
        assertEquals("test_token_1", activeTokens.get(0).getFcmToken());
        // expired_token은 비활성이므로 포함되지 않음
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회 시 빈 리스트 반환 테스트")
    void testEmptyListForNonExistentUser() {
        // when
        List<FcmToken> tokens = fcmTokenRepository.findByUser_IdAndIsActiveTrue(999L);

        // then
        assertTrue(tokens.isEmpty());
    }

    @Test
    @DisplayName("사용자의 모든 토큰 비활성화 테스트")
    void testDeactivateAllTokensByUserId() {
        // when
        fcmTokenRepository.deactivateAllTokensByUserId(testUser1.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        List<FcmToken> activeTokens = fcmTokenRepository.findByUser_IdAndIsActiveTrue(testUser1.getId());
        assertTrue(activeTokens.isEmpty());
    }

    @Test
    @DisplayName("특정 토큰 비활성화 테스트")
    void testDeactivateByToken() {
        // when
        fcmTokenRepository.deactivateByToken("test_token_1");
        entityManager.flush();
        entityManager.clear();

        // then
        FcmToken found = fcmTokenRepository.findByUser_IdAndFcmToken(testUser1.getId(), "test_token_1").orElse(null);
        assertNotNull(found);
        assertFalse(found.getIsActive());
    }

    @Test
    @DisplayName("사용자별 활성 토큰 개수 조회 테스트")
    void testCountByUserIdAndIsActiveTrue() {
        // when
        long count = fcmTokenRepository.countByUser_IdAndIsActiveTrue(testUser1.getId());

        // then
        assertEquals(1, count); // testToken1만 활성 상태
    }

    @Test
    @DisplayName("토큰 저장 및 업데이트 테스트")
    void testSaveAndUpdateToken() {
        // given - 새로운 테스트 사용자 생성
        User testUser3 = User.builder()
                .nickname("testUser3")
                .build();
        testUser3 = entityManager.persistAndFlush(testUser3);

        FcmToken newToken = FcmToken.builder()
                .user(testUser3)
                .fcmToken("new_test_token")
                .platform(FcmToken.Platform.WEB)
                .isActive(true)
                .build();

        // when
        FcmToken saved = fcmTokenRepository.save(newToken);

        // then
        assertNotNull(saved.getTokenId());
        assertEquals("new_test_token", saved.getFcmToken());
        assertEquals(FcmToken.Platform.WEB, saved.getPlatform());

        // 토큰 업데이트
        saved.deactivate();
        FcmToken updated = fcmTokenRepository.save(saved);

        // then
        assertFalse(updated.getIsActive());
    }
}