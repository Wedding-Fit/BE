package com.weddingfit.dto.response.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingAnalyticsResponse {
    
    @JsonProperty("totalCost")
    private Long totalCost;
    
    @JsonProperty("aiMessage")
    private String aiMessage;
    
    @JsonProperty("food")
    private CategorySpending food;
    
    @JsonProperty("culture")
    private CategorySpending culture;
    
    @JsonProperty("medical")
    private CategorySpending medical;
    
    @JsonProperty("transport")
    private CategorySpending transport;
    
    @JsonProperty("shopping")
    private CategorySpending shopping;
    
    @JsonProperty("education")
    private CategorySpending education;
    
    @JsonProperty("communication")
    private CategorySpending communication;
    
    @JsonProperty("etc")
    private CategorySpending etc;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySpending {
        @JsonProperty("cost")
        private Long cost;
        
        @JsonProperty("average")
        private Long average;
    }
}