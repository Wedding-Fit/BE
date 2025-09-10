package com.weddingfit.dto.request.fcm;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MessagePushRequest {
    private final boolean validateOnly;
    private final MessageRequest message;

    public static MessagePushRequest of(MessagePushServiceRequest req) {
        return MessagePushRequest.builder()
                .validateOnly(false)
                .message(MessageRequest.of(req))
                .build();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static class MessageRequest {
        private final NotificationRequest notification;
        private final String token;

        public static MessageRequest of(MessagePushServiceRequest req) {
            return MessageRequest.builder()
                    .notification(NotificationRequest.of(req))
                    .token(req.targetToken())
                    .build();
        }
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static class NotificationRequest {
        private final String title;
        private final String body;

        public static NotificationRequest of(MessagePushServiceRequest req) {
            return NotificationRequest.builder()
                    .title(req.title())
                    .body(req.body())
                    .build();
        }
    }
}
