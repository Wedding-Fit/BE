package com.weddingfit.repository.fcm;

import com.weddingfit.entity.fcm.FcmToken;
import com.weddingfit.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    /**
     * 특정 Goal ID에 해당하는 커플의 활성 FCM 토큰들 조회
     */
    @Query(value = """
        SELECT ft.fcm_token 
        FROM fcm_tokens ft
        JOIN users u ON ft.user_id = u.user_id
        JOIN couples c ON (c.user1_id = u.user_id OR c.user2_id = u.user_id)
        JOIN savings_goals g ON g.couple_id = c.couple_id
        WHERE g.goal_id = :goalId 
          AND ft.is_active = true
        """, nativeQuery = true)
    List<String> findActiveTokensByGoalId(@Param("goalId") Long goalId);

    /**
     * 사용자별 활성 토큰 목록 조회
     */
    List<FcmToken> findByUser_IdAndIsActiveTrue(Long userId);

    /**
     * 특정 사용자의 특정 토큰 조회
     */
    Optional<FcmToken> findByUser_IdAndFcmToken(Long userId, String fcmToken);

    /**
     * 사용자의 모든 토큰 비활성화
     */
    @Modifying
    @Query("UPDATE FcmToken t SET t.isActive = false WHERE t.user.id = :userId")
    void deactivateAllTokensByUserId(@Param("userId") Long userId);

    /**
     * 특정 토큰 비활성화
     */
    @Modifying
    @Query("UPDATE FcmToken t SET t.isActive = false WHERE t.fcmToken = :token")
    void deactivateByToken(@Param("token") String token);

    /**
     * 사용자별 활성 토큰 개수
     */
    long countByUser_IdAndIsActiveTrue(Long userId);

    /**
     * User 엔티티로 사용자의 활성 토큰 조회
     */
    List<FcmToken> findByUserAndIsActiveTrue(User user);

    /**
     * User 엔티티와 토큰으로 특정 토큰 조회
     */
    Optional<FcmToken> findByUserAndFcmToken(User user, String fcmToken);

    /**
     * 토큰 ID와 User로 토큰 조회
     */
    Optional<FcmToken> findByTokenIdAndUser(Long tokenId, User user);
}