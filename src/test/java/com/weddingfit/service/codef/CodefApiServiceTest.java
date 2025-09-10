package com.weddingfit.service.codef;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodefApiService 테스트")
class CodefApiServiceTest {

    @Mock
    private CodefAuthService codefAuthService;
    
    @Mock
    private WebClient webClient;
    
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private CodefApiService codefApiService;

    @BeforeEach
    void setUp() {
        // WebClient를 목 객체로 주입
        ReflectionTestUtils.setField(codefApiService, "webClient", webClient);
        ReflectionTestUtils.setField(codefApiService, "baseUrl", "https://development.codef.io");
        
        // 테스트용 유효한 RSA 공개키 (실제 테스트용 키)
        String testPublicKey = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs1WAkf0Ph2gie0I6eXoN\n" +
            "O/2gGE81fvybfc1NvGyGWKx9saTVJ7h9lg9cjSnksXY2qN0YPxtl6PlC6yUOz52G\n" +
            "H6HqHx1Sph0zXZvHZXe52XLC8SHrb8T0ls/yM3LDEqZp8QEhXEDaNpXtC0nU6I1s\n" +
            "Df3bt3cwD/Rj4s9kDGOWnA74CCmywbDNWXPaE4WVM+tUjKaTH+75KvFfQptNEZf1\n" +
            "d6DJy1j4o8F0Tt8nUK7q53nHnpuiMBiB5mx8Ga6tlFplIELiL6KKfs/PQM7Z3v4q\n" +
            "ekGho9T68FXLBh0ZPKAwIw04VZMUZaNhmlplHCSHKQIkjDTYnCAN31Psb1xyYW+2\n" +
            "PQIDAQAB\n" +
            "-----END PUBLIC KEY-----";
        ReflectionTestUtils.setField(codefApiService, "publicKey", testPublicKey);
    }

    @Test
    @DisplayName("계좌 등록 성공 테스트")
    void registerAccount_Success() {
        // Given
        String bankCode = "0004";
        String loginId = "testuser123";
        String password = "testpass123";
        String accessToken = "test-access-token";
        String connectedId = "connected-id-123";

        String mockResponse = "{\n" +
                "  \"result\": {\n" +
                "    \"code\": \"CF-00000\",\n" +
                "    \"message\": \"정상\",\n" +
                "    \"transactionId\": \"tx-123\"\n" +
                "  },\n" +
                "  \"data\": {\n" +
                "    \"connectedId\": \"" + connectedId + "\",\n" +
                "    \"successList\": [],\n" +
                "    \"errorList\": []\n" +
                "  }\n" +
                "}";

        given(codefAuthService.getValidAccessToken()).willReturn(accessToken);
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
        given(requestBodySpec.headers(any())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockResponse));

        // When
        CodefApiService.CodefConnectResponse result = codefApiService.registerAccount(bankCode, loginId, password);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getResult().getCode()).isEqualTo("CF-00000"),
            () -> assertThat(result.getResult().getMessage()).isEqualTo("정상"),
            () -> assertThat(result.getData().getConnectedId()).isEqualTo(connectedId)
        );

        verify(codefAuthService).getValidAccessToken();
    }

    @Test
    @DisplayName("계좌 등록 실패 테스트 - 잘못된 인증정보")
    void registerAccount_Fail_InvalidCredentials() {
        // Given
        String bankCode = "0004";
        String loginId = "invalid";
        String password = "invalid";
        String accessToken = "test-access-token";

        String mockErrorResponse = "{\n" +
                "  \"result\": {\n" +
                "    \"code\": \"CF-03002\",\n" +
                "    \"message\": \"로그인 실패\",\n" +
                "    \"transactionId\": \"tx-456\"\n" +
                "  },\n" +
                "  \"data\": {\n" +
                "    \"connectedId\": null,\n" +
                "    \"successList\": [],\n" +
                "    \"errorList\": [{\n" +
                "      \"code\": \"CF-03002\",\n" +
                "      \"message\": \"로그인 실패\"\n" +
                "    }]\n" +
                "  }\n" +
                "}";

        given(codefAuthService.getValidAccessToken()).willReturn(accessToken);
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
        given(requestBodySpec.headers(any())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockErrorResponse));

        // When
        CodefApiService.CodefConnectResponse result = codefApiService.registerAccount(bankCode, loginId, password);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getResult().getCode()).isEqualTo("CF-03002"),
            () -> assertThat(result.getResult().getMessage()).isEqualTo("로그인 실패")
        );
    }

    @Test
    @DisplayName("거래내역 조회 성공 테스트")
    void getTransactionHistory_Success() {
        // Given
        String connectedId = "connected-id-123";
        String accountNumber = "110-123-456789";
        String startDate = "20240801";
        String endDate = "20240831";
        String accessToken = "test-access-token";

        String mockResponse = "{\n" +
                "  \"result\": {\n" +
                "    \"code\": \"CF-00000\",\n" +
                "    \"message\": \"정상\",\n" +
                "    \"transactionId\": \"tx-789\"\n" +
                "  },\n" +
                "  \"data\": {\n" +
                "    \"resAccountBalance\": \"1000000\",\n" +
                "    \"resAccountDisplay\": \"110-***-***789\",\n" +
                "    \"resAccountName\": \"입출금통장\",\n" +
                "    \"resAccountHolder\": \"홍길동\",\n" +
                "    \"resTrHistoryList\": [{\n" +
                "      \"resAccountTrDate\": \"20240830\",\n" +
                "      \"resAccountTrTime\": \"143022\",\n" +
                "      \"resAccountDesc3\": \"스타벅스강남점\",\n" +
                "      \"resAccountOut\": \"5500\",\n" +
                "      \"resAccountIn\": \"0\",\n" +
                "      \"resAfterTranBalance\": \"994500\"\n" +
                "    }]\n" +
                "  }\n" +
                "}";

        given(codefAuthService.getValidAccessToken()).willReturn(accessToken);
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
        given(requestBodySpec.headers(any())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockResponse));

        // When
        CodefApiService.CodefTransactionResponse result = codefApiService
                .getTransactionHistory(connectedId, accountNumber, "0004", startDate, endDate);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getResult().getCode()).isEqualTo("CF-00000"),
            () -> assertThat(result.getData()).isNotNull(),
            () -> assertThat(result.getData().getResAccountBalance()).isEqualTo("1000000"),
            () -> assertThat(result.getData().getResTrHistoryList()).hasSize(1)
        );
    }

    @Test
    @DisplayName("거래내역 조회 실패 테스트 - 존재하지 않는 connectedId")
    void getTransactionHistory_Fail_InvalidConnectedId() {
        // Given
        String invalidConnectedId = "invalid-connected-id";
        String accountNumber = "110-123-456789";
        String startDate = "20240801";
        String endDate = "20240831";
        String accessToken = "test-access-token";

        String mockErrorResponse = "{\n" +
                "  \"result\": {\n" +
                "    \"code\": \"CF-03001\",\n" +
                "    \"message\": \"등록되지 않은 connectedId입니다\",\n" +
                "    \"transactionId\": \"tx-error-123\"\n" +
                "  }\n" +
                "}";

        given(codefAuthService.getValidAccessToken()).willReturn(accessToken);
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
        given(requestBodySpec.headers(any())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockErrorResponse));

        // When
        CodefApiService.CodefTransactionResponse result = codefApiService
                .getTransactionHistory(invalidConnectedId, accountNumber, "0004", startDate, endDate);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getResult().getCode()).isEqualTo("CF-03001"),
            () -> assertThat(result.getResult().getMessage()).isEqualTo("등록되지 않은 connectedId입니다")
        );
    }

    @Test
    @DisplayName("API 호출 시 예외 처리 테스트")
    void registerAccount_Exception_Handling() {
        // Given
        String bankCode = "0004";
        String loginId = "testuser";
        String password = "testpass";
        String accessToken = "test-access-token";

        given(codefAuthService.getValidAccessToken()).willReturn(accessToken);
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
        given(requestBodySpec.headers(any())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(String.class))
                .willReturn(Mono.error(new RuntimeException("Network error")));

        // When
        CodefApiService.CodefConnectResponse result = codefApiService.registerAccount(bankCode, loginId, password);

        // Then
        assertThat(result).isNull();
    }
}