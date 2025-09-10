package com.weddingfit.dto.response.fcm;

import com.weddingfit.entity.fcm.FcmToken;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FcmTokenResponse(
        Long tokenId,
        String fcmToken,
        FcmToken.Platform platform,
        String deviceId,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FcmTokenResponse from(FcmToken fcmToken) {
        return FcmTokenResponse.builder()
                .tokenId(fcmToken.getTokenId())
                .fcmToken(fcmToken.getFcmToken())
                .platform(fcmToken.getPlatform())
                .deviceId(fcmToken.getDeviceId())
                .isActive(fcmToken.getIsActive())
                .createdAt(fcmToken.getCreatedAt())
                .updatedAt(fcmToken.getUpdatedAt())
                .build();
    }
}