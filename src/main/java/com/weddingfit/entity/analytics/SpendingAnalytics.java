package com.weddingfit.entity.analytics;

import com.weddingfit.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "spending_analytics", 
       indexes = {
           @Index(name = "idx_spending_analytics_user_date", columnList = "user_id,analysis_date")
       })
public class SpendingAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analytics_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "analysis_date", nullable = false)
    private LocalDate analysisDate;
    
    @Column(name = "category", nullable = false)
    private String category;
    
    @Column(name = "total_cost", nullable = false)
    private Long totalCost;
    
    @Column(name = "ai_message", columnDefinition = "TEXT")
    private String aiMessage;
    
    @Column(name = "food_cost", nullable = false)
    private Long foodCost;
    
    @Column(name = "food_average", nullable = false)
    private Long foodAverage;
    
    @Column(name = "culture_cost", nullable = false)
    private Long cultureCost;
    
    @Column(name = "culture_average", nullable = false)
    private Long cultureAverage;
    
    @Column(name = "medical_cost", nullable = false)
    private Long medicalCost;
    
    @Column(name = "medical_average", nullable = false)
    private Long medicalAverage;
    
    @Column(name = "transport_cost", nullable = false)
    private Long transportCost;
    
    @Column(name = "transport_average", nullable = false)
    private Long transportAverage;
    
    @Column(name = "shopping_cost", nullable = false)
    private Long shoppingCost;
    
    @Column(name = "shopping_average", nullable = false)
    private Long shoppingAverage;
    
    @Column(name = "education_cost", nullable = false)
    private Long educationCost;
    
    @Column(name = "education_average", nullable = false)
    private Long educationAverage;
    
    @Column(name = "communication_cost", nullable = false)
    private Long communicationCost;
    
    @Column(name = "communication_average", nullable = false)
    private Long communicationAverage;
    
    @Column(name = "etc_cost", nullable = false)
    private Long etcCost;
    
    @Column(name = "etc_average", nullable = false)
    private Long etcAverage;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}