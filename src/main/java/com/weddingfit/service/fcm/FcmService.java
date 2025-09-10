package com.weddingfit.service.fcm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.weddingfit.dto.request.fcm.MessagePushRequest;
import com.weddingfit.dto.request.fcm.MessagePushServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final ObjectMapper objectMapper;

    @Value("${fcm.file_path}")
    private Resource firebaseKey;

    @Value("${fcm.url}")
    private String firebaseApiUri;

    @Value("${fcm.google_api}")
    private String googleApiScope;

    public void pushMessage(final MessagePushServiceRequest req) {
        String messageBody = makeMessage(req);
        String accessToken = getAccessToken();
        
        log.info("FCM 요청 URL: {}", firebaseApiUri);
        log.info("FCM 요청 본문: {}", messageBody);
        log.info("FCM 토큰 앞 20자리: {}", req.targetToken().substring(0, Math.min(20, req.targetToken().length())));
        
        RestClient.create()
                .post()
                .uri(firebaseApiUri)
                .contentType(APPLICATION_JSON)
                .body(messageBody)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .header(ACCEPT, "application/json; charset=UTF-8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (rqt, rsp) -> {
                    log.error("FCM 4xx 오류: {} - URL: {}", rsp.getStatusCode(), firebaseApiUri);
                    throw new IllegalStateException("FCM 4xx: " + rsp.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (rqt, rsp) -> {
                    log.error("FCM 5xx 오류: {}", rsp.getStatusCode());
                    throw new IllegalStateException("FCM 5xx: " + rsp.getStatusCode());
                })
                .toBodilessEntity();
        
        log.info("FCM 메시지 전송 성공");
    }

    private String makeMessage(MessagePushServiceRequest req) {
        try {
            return objectMapper.writeValueAsString(MessagePushRequest.of(req));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("FCM message serialize error", e);
        }
    }

    private String getAccessToken() {
        try (var in = firebaseKey.getInputStream()) {
            var creds = GoogleCredentials.fromStream(in)
                    .createScoped(List.of(googleApiScope));
            creds.refreshIfExpired();
            return creds.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new IllegalStateException("FCM credentials load error", e);
        }
    }
}

