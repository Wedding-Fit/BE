package com.weddingfit.service.codef;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@Component
public class CodefAuthClient {

    private static final Logger logger = LoggerFactory.getLogger(CodefAuthClient.class);
    private final WebClient webClient;
    
    @Value("${codef.base-url}")
    private String baseUrl;
    
    @Value("${codef.client-id}")
    private String clientId;
    
    @Value("${codef.client-secret}")
    private String clientSecret;
    
    @Value("${codef.timeout-ms}")
    private int timeoutMs;

    public CodefAuthClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    /**
     * CODEF API access_token 발급
     * @return CodefTokenResponse (access_token, expires_in 포함)
     */
    public CodefTokenResponse getAccessToken() {
        logger.info("CODEF 토큰 요청 시작 - URL: https://oauth.codef.io/oauth/token");
        logger.debug("클라이언트 ID: {}, 타임아웃: {}ms", clientId, timeoutMs);
        
        // form-urlencoded 형식으로 요청 데이터 구성
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("scope", "read");

        try {
            CodefTokenResponse response = webClient.post()
                    .uri("https://oauth.codef.io/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                    .body(BodyInserters.fromFormData(requestBody))
                    .retrieve()
                    .bodyToMono(CodefTokenResponse.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .doOnSuccess(res -> logger.info("CODEF 토큰 요청 성공"))
                    .doOnError(error -> logger.error("CODEF 토큰 요청 실패: {}", error.getMessage()))
                    .block();
            
            logger.debug("토큰 응답: access_token={}, expires_in={}", 
                    response != null ? "***" : "null", 
                    response != null ? response.getExpires_in() : "null");
            
            return response;
        } catch (Exception e) {
            logger.error("CODEF 토큰 요청 중 예외 발생", e);
            throw e;
        }
    }

    /**
     * CODEF 토큰 응답 DTO
     */
    public static class CodefTokenResponse {
        private String access_token;
        private String token_type;
        private int expires_in;
        private String scope;

        // Getters & Setters
        public String getAccess_token() { return access_token; }
        public void setAccess_token(String access_token) { this.access_token = access_token; }
        
        public String getToken_type() { return token_type; }
        public void setToken_type(String token_type) { this.token_type = token_type; }
        
        public int getExpires_in() { return expires_in; }
        public void setExpires_in(int expires_in) { this.expires_in = expires_in; }
        
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
    }
}