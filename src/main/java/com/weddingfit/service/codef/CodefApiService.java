package com.weddingfit.service.codef;

import com.weddingfit.global.util.RsaEncryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class CodefApiService {

    private static final Logger logger = LoggerFactory.getLogger(CodefApiService.class);
    private final WebClient webClient;
    private final CodefAuthService codefAuthService;

    @Value("${codef.base-url}")
    private String baseUrl;

    @Value("${codef.public-key}")
    private String publicKey;

    @Autowired
    public CodefApiService(CodefAuthService codefAuthService) {
        this.webClient = WebClient.builder().build();
        this.codefAuthService = codefAuthService;
    }

    /**
     * 계좌 등록 (connectId 발급)
     * @param bankCode 은행코드 (예: "0004" - 국민은행)
     * @param loginId 인터넷뱅킹 아이디
     * @param password 인터넷뱅킹 비밀번호
     * @return CodefConnectResponse (connectedId 포함)
     */
    public CodefConnectResponse registerAccount(String bankCode, String loginId, String password) {
        // 1. 유효한 Access Token 조회 (자동 갱신 포함)
        String accessToken = codefAuthService.getValidAccessToken();

        // 2. 비밀번호 RSA 암호화
        String encryptedPassword = RsaEncryptUtil.encryptRSA(password, publicKey);

        // 3. 요청 파라미터 설정
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("accountList", new Object[]{
            new HashMap<String, Object>() {{
                put("countryCode", "KR");
                put("businessType", "BK");
                put("clientType", "P");
                put("organization", bankCode);
                put("loginType", "1");
                put("id", loginId);
                put("password", encryptedPassword);
            }}
        });

        // 4. API 호출 - Codef는 URL 인코딩된 응답을 반환하므로 String으로 먼저 받음
        try {
            String rawResponse = webClient.post()
                    .uri("https://development.codef.io/v1/account/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (rawResponse == null) {
                logger.error("Codef API 응답이 null입니다");
                return null;
            }
            
            logger.debug("Codef 원본 응답: {}", rawResponse);
            
            // URL 디코딩
            String decodedResponse = java.net.URLDecoder.decode(rawResponse, "UTF-8");
            logger.debug("Codef 디코딩된 응답: {}", decodedResponse);
            
            // JSON 파싱 - 알 수 없는 필드 무시
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(decodedResponse, CodefConnectResponse.class);
            
        } catch (Exception e) {
            logger.error("Codef API 호출 또는 응답 처리 중 오류: ", e);
            return null;
        }
    }

    /**
     * 거래내역 조회
     * @param connectedId 등록된 계좌의 connectedId
     * @param startDate 조회 시작일 (YYYYMMDD)
     * @param endDate 조회 종료일 (YYYYMMDD)
     * @return CodefTransactionResponse (거래내역 포함)
     */
    public CodefTransactionResponse getTransactionHistory(String connectedId, String startDate, String endDate) {
        // 1. 유효한 Access Token 조회 (자동 갱신 포함)
        String accessToken = codefAuthService.getValidAccessToken();

        // 2. 요청 파라미터 설정
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("connectedId", connectedId);
        requestBody.put("countryCode", "KR");
        requestBody.put("businessType", "BK");
        requestBody.put("clientType", "P");
        requestBody.put("organization", "0004"); // 국민은행 (실제로는 동적으로 설정)
        requestBody.put("loginType", "1");
        requestBody.put("inquiryType", "0");
        requestBody.put("commStartDate", startDate);
        requestBody.put("commEndDate", endDate);

        // 3. API 호출 - Codef는 URL 인코딩된 응답을 반환하므로 String으로 먼저 받음
        try {
            String rawResponse = webClient.post()
                    .uri("https://development.codef.io/v1/kr/bank/p/account/transaction-list")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (rawResponse == null) {
                logger.error("Codef 거래내역 API 응답이 null입니다");
                return null;
            }
            
            logger.debug("Codef 거래내역 원본 응답: {}", rawResponse);
            
            // URL 디코딩
            String decodedResponse = java.net.URLDecoder.decode(rawResponse, "UTF-8");
            logger.debug("Codef 거래내역 디코딩된 응답: {}", decodedResponse);
            
            // JSON 파싱
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.readValue(decodedResponse, CodefTransactionResponse.class);
            
        } catch (Exception e) {
            logger.error("Codef 거래내역 API 호출 또는 응답 처리 중 오류: ", e);
            return null;
        }
    }

    // DTO 클래스들
    public static class CodefConnectResponse {
        private Result result;
        private Data data;
        private String connectedId;

        // Getters & Setters
        public Result getResult() { return result; }
        public void setResult(Result result) { this.result = result; }
        
        public Data getData() { return data; }
        public void setData(Data data) { this.data = data; }
        
        public String getConnectedId() { return connectedId; }
        public void setConnectedId(String connectedId) { this.connectedId = connectedId; }

        public static class Result {
            private String code;
            private String message;
            private String transactionId;

            // Getters & Setters
            public String getCode() { return code; }
            public void setCode(String code) { this.code = code; }
            
            public String getMessage() { return message; }
            public void setMessage(String message) { this.message = message; }
            
            public String getTransactionId() { return transactionId; }
            public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        }

        public static class Data {
            // 필요에 따라 추가
        }
    }

    public static class CodefTransactionResponse {
        private Result result;
        private String clientType;
        private TransactionData data;
        private String connectedId;

        // Getters & Setters
        public Result getResult() { return result; }
        public void setResult(Result result) { this.result = result; }
        
        public String getClientType() { return clientType; }
        public void setClientType(String clientType) { this.clientType = clientType; }
        
        public TransactionData getData() { return data; }
        public void setData(TransactionData data) { this.data = data; }
        
        public String getConnectedId() { return connectedId; }
        public void setConnectedId(String connectedId) { this.connectedId = connectedId; }

        public static class Result {
            private String code;
            private String message;
            private String transactionId;

            // Getters & Setters
            public String getCode() { return code; }
            public void setCode(String code) { this.code = code; }
            
            public String getMessage() { return message; }
            public void setMessage(String message) { this.message = message; }
            
            public String getTransactionId() { return transactionId; }
            public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        }

        public static class TransactionData {
            private String resAccountBalance;
            private String resAccountDisplay;
            private String resAccountName;
            private String resAccountHolder;
            private Object[] resTrHistoryList;

            // Getters & Setters
            public String getResAccountBalance() { return resAccountBalance; }
            public void setResAccountBalance(String resAccountBalance) { this.resAccountBalance = resAccountBalance; }
            
            public String getResAccountDisplay() { return resAccountDisplay; }
            public void setResAccountDisplay(String resAccountDisplay) { this.resAccountDisplay = resAccountDisplay; }
            
            public String getResAccountName() { return resAccountName; }
            public void setResAccountName(String resAccountName) { this.resAccountName = resAccountName; }
            
            public String getResAccountHolder() { return resAccountHolder; }
            public void setResAccountHolder(String resAccountHolder) { this.resAccountHolder = resAccountHolder; }
            
            public Object[] getResTrHistoryList() { return resTrHistoryList; }
            public void setResTrHistoryList(Object[] resTrHistoryList) { this.resTrHistoryList = resTrHistoryList; }
        }
    }
}