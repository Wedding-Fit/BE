package com.weddingfit.dto.request.fcm;

import lombok.Builder;
import lombok.AccessLevel;

@Builder(access = AccessLevel.PUBLIC)
public record MessagePushServiceRequest(
        String targetToken,
        String title,
        String body
) {
    public static MessagePushServiceRequest of(String token, String title, String body) {
        return MessagePushServiceRequest.builder()
                .targetToken(token)
                .title(title)
                .body(body)
                .build();
    }
}

