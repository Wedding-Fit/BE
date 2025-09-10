package com.weddingfit.service.fcm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.weddingfit.dto.request.fcm.MessagePushRequest;
import com.weddingfit.dto.request.fcm.MessagePushServiceRequest;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class FcmService {

    private final ObjectMapper objectMapper;

    // classpath: 또는 file: 모두 지원 (Resource로 주입)
    @Value("${fcm.file_path}")
    private Resource firebaseKey;

    @Value("${fcm.url}")
    private String firebaseApiUri;

    @Value("${fcm.google_api}")
    private String googleApiScope;

    public void pushMessage(final MessagePushServiceRequest req) {
        RestClient.create()
                .post()
                .uri(firebaseApiUri)
                .contentType(APPLICATION_JSON)
                .body(makeMessage(req))
                .header(AUTHORIZATION, "Bearer " + getAccessToken())
                .header(ACCEPT, "application/json; charset=UTF-8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (rqt, rsp) -> {
                    throw new IllegalStateException("FCM 4xx: " + rsp.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (rqt, rsp) -> {
                    throw new IllegalStateException("FCM 5xx: " + rsp.getStatusCode());
                })
                .toBodilessEntity();
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

