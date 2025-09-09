package com.weddingfit.service.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weddingfit.entity.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {
    
    @Value("${openai.api-key}")
    private String apiKey;
    
    @Value("${openai.base-url}")
    private String baseUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String generateSpendingAdvice(Map<Transaction.TransactionCategory, Long> categorySpending, 
                                       Map<Transaction.TransactionCategory, Long> categoryAverages,
                                       long totalCost) {
        try {
            String prompt = buildSpendingAnalysisPrompt(categorySpending, categoryAverages, totalCost);
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(List.of(
                            ChatMessage.builder()
                                    .role("system")
                                    .content("당신은 가계부 분석 전문가입니다. 사용자의 소비 패턴을 분석하고 실용적인 절약 조언을 제공해주세요. 답변은 한국어로 2-3문장 이내로 친근하게 작성해주세요.")
                                    .build(),
                            ChatMessage.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .maxTokens(150)
                    .temperature(0.7)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<ChatCompletionRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<ChatCompletionResponse> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions", 
                    entity, 
                    ChatCompletionResponse.class
            );
            
            if (response.getBody() != null && 
                response.getBody().getChoices() != null && 
                !response.getBody().getChoices().isEmpty()) {
                
                String aiMessage = response.getBody().getChoices().get(0).getMessage().getContent();
                log.info("OpenAI API 응답 성공: {}", aiMessage);
                return aiMessage;
            }
            
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패", e);
        }
        
        // 실패 시 기본 메시지 반환
        return generateFallbackMessage(categorySpending, categoryAverages);
    }
    
    private String buildSpendingAnalysisPrompt(Map<Transaction.TransactionCategory, Long> categorySpending,
                                             Map<Transaction.TransactionCategory, Long> categoryAverages,
                                             long totalCost) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("사용자의 최근 30일 소비 패턴을 분석해주세요.\n");
        prompt.append("총 지출: ").append(String.format("%,d", totalCost)).append("원\n\n");
        
        prompt.append("카테고리별 지출 내역:\n");
        for (Map.Entry<Transaction.TransactionCategory, Long> entry : categorySpending.entrySet()) {
            String categoryName = getCategoryKoreanName(entry.getKey());
            long cost = entry.getValue();
            long average = categoryAverages.getOrDefault(entry.getKey(), 0L);
            
            prompt.append("- ").append(categoryName).append(": ")
                  .append(String.format("%,d", cost)).append("원 ")
                  .append("(평균: ").append(String.format("%,d", average)).append("원)\n");
        }
        
        prompt.append("\n가장 절약할 수 있는 부분과 실용적인 조언을 제공해주세요.");
        
        return prompt.toString();
    }
    
    private String getCategoryKoreanName(Transaction.TransactionCategory category) {
        return switch (category) {
            case FOOD -> "식비";
            case TRANSPORT -> "교통비";
            case SHOPPING -> "쇼핑";
            case CULTURE -> "문화생활";
            case MEDICAL -> "의료비";
            case EDU -> "교육비";
            case TELECOM -> "통신비";
            case ETC -> "기타";
        };
    }
    
    private String generateFallbackMessage(Map<Transaction.TransactionCategory, Long> categorySpending,
                                         Map<Transaction.TransactionCategory, Long> categoryAverages) {
        StringBuilder message = new StringBuilder();
        
        // 가장 많이 지출한 카테고리 찾기
        Transaction.TransactionCategory maxCategory = categorySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Transaction.TransactionCategory.ETC);
        
        // 평균보다 많이 지출한 카테고리들 찾기
        for (Map.Entry<Transaction.TransactionCategory, Long> entry : categorySpending.entrySet()) {
            Transaction.TransactionCategory category = entry.getKey();
            long cost = entry.getValue();
            long average = categoryAverages.getOrDefault(category, 0L);
            
            if (cost > average * 1.5) {
                switch (category) {
                    case FOOD:
                        message.append("식비 지출이 평균보다 높아요. 배달 음식을 줄여보세요! ");
                        break;
                    case TRANSPORT:
                        message.append("교통비가 과하게 지출되고 있어요. 대중교통을 이용해 절약해보세요. ");
                        break;
                    case SHOPPING:
                        message.append("쇼핑 지출이 많네요. 필요한 것만 구매하는 습관을 들여보세요. ");
                        break;
                    case CULTURE:
                        message.append("문화생활비가 높아요. 무료 문화 프로그램을 활용해보세요. ");
                        break;
                }
            }
        }
        
        if (message.length() == 0) {
            message.append("전반적으로 균형잡힌 소비 패턴을 보이고 있어요. 계속 유지해보세요!");
        }
        
        return message.toString().trim();
    }
    
    // DTO classes
    public static class ChatCompletionRequest {
        private String model;
        private List<ChatMessage> messages;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        private Double temperature;
        
        public static ChatCompletionRequestBuilder builder() {
            return new ChatCompletionRequestBuilder();
        }
        
        // Getters and setters
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public List<ChatMessage> getMessages() { return messages; }
        public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
        public Integer getMaxTokens() { return maxTokens; }
        public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
        
        public static class ChatCompletionRequestBuilder {
            private String model;
            private List<ChatMessage> messages;
            private Integer maxTokens;
            private Double temperature;
            
            public ChatCompletionRequestBuilder model(String model) { this.model = model; return this; }
            public ChatCompletionRequestBuilder messages(List<ChatMessage> messages) { this.messages = messages; return this; }
            public ChatCompletionRequestBuilder maxTokens(Integer maxTokens) { this.maxTokens = maxTokens; return this; }
            public ChatCompletionRequestBuilder temperature(Double temperature) { this.temperature = temperature; return this; }
            
            public ChatCompletionRequest build() {
                ChatCompletionRequest request = new ChatCompletionRequest();
                request.setModel(model);
                request.setMessages(messages);
                request.setMaxTokens(maxTokens);
                request.setTemperature(temperature);
                return request;
            }
        }
    }
    
    public static class ChatMessage {
        private String role;
        private String content;
        
        public static ChatMessageBuilder builder() {
            return new ChatMessageBuilder();
        }
        
        // Getters and setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public static class ChatMessageBuilder {
            private String role;
            private String content;
            
            public ChatMessageBuilder role(String role) { this.role = role; return this; }
            public ChatMessageBuilder content(String content) { this.content = content; return this; }
            
            public ChatMessage build() {
                ChatMessage message = new ChatMessage();
                message.setRole(role);
                message.setContent(content);
                return message;
            }
        }
    }
    
    public static class ChatCompletionResponse {
        private List<Choice> choices;
        
        public List<Choice> getChoices() { return choices; }
        public void setChoices(List<Choice> choices) { this.choices = choices; }
        
        public static class Choice {
            private ChatMessage message;
            
            public ChatMessage getMessage() { return message; }
            public void setMessage(ChatMessage message) { this.message = message; }
        }
    }
}