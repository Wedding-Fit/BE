package com.weddingfit.dto.request.fcm;

import com.weddingfit.entity.fcm.FcmToken;
import lombok.Builder;

@Builder
public record FcmTokenRegisterRequest(
        String fcmToken,
        FcmToken.Platform platform,
        String deviceId
) {
    public static FcmTokenRegisterRequest of(String fcmToken, FcmToken.Platform platform, String deviceId) {
        return FcmTokenRegisterRequest.builder()
                .fcmToken(fcmToken)
                .platform(platform)
                .deviceId(deviceId)
                .build();
    }
}