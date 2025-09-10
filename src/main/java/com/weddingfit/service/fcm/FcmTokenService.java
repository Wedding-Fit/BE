package com.weddingfit.service.fcm;

import com.weddingfit.dto.request.fcm.FcmTokenRegisterRequest;
import com.weddingfit.dto.response.fcm.FcmTokenResponse;
import com.weddingfit.entity.fcm.FcmToken;
import com.weddingfit.entity.user.User;
import com.weddingfit.repository.fcm.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public FcmTokenResponse registerToken(User user, FcmTokenRegisterRequest request) {
        log.info("사용자 {}의 FCM 토큰 등록 시작: {}", user.getId(), 
                request.fcmToken().substring(0, Math.min(20, request.fcmToken().length())));

        // 기존에 같은 토큰이 있다면 활성화
        Optional<FcmToken> existingToken = fcmTokenRepository.findByUserAndFcmToken(user, request.fcmToken());
        if (existingToken.isPresent()) {
            FcmToken token = existingToken.get();
            token.activate();
            log.info("기존 FCM 토큰 활성화: {}", token.getTokenId());
            return FcmTokenResponse.from(token);
        }

        // 새로운 토큰 등록
        FcmToken newToken = FcmToken.builder()
                .user(user)
                .fcmToken(request.fcmToken())
                .platform(request.platform())
                .deviceId(request.deviceId())
                .isActive(true)
                .build();

        FcmToken savedToken = fcmTokenRepository.save(newToken);
        log.info("새로운 FCM 토큰 등록 완료: {}", savedToken.getTokenId());
        
        return FcmTokenResponse.from(savedToken);
    }

    public List<FcmTokenResponse> getActiveTokensByUser(User user) {
        List<FcmToken> activeTokens = fcmTokenRepository.findByUserAndIsActiveTrue(user);
        return activeTokens.stream()
                .map(FcmTokenResponse::from)
                .toList();
    }

    public List<String> getActiveTokenStringsByUser(User user) {
        List<FcmToken> activeTokens = fcmTokenRepository.findByUserAndIsActiveTrue(user);
        return activeTokens.stream()
                .map(FcmToken::getFcmToken)
                .toList();
    }

    @Transactional
    public void deactivateToken(User user, Long tokenId) {
        FcmToken token = fcmTokenRepository.findByTokenIdAndUser(tokenId, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 FCM 토큰을 찾을 수 없습니다"));
        
        token.deactivate();
        log.info("FCM 토큰 비활성화: {}", tokenId);
    }

    @Transactional
    public void deactivateTokensByUser(User user) {
        List<FcmToken> activeTokens = fcmTokenRepository.findByUserAndIsActiveTrue(user);
        activeTokens.forEach(FcmToken::deactivate);
        log.info("사용자 {}의 모든 FCM 토큰 비활성화", user.getId());
    }
}