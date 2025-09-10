package com.weddingfit.entity.couple;

import com.weddingfit.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "couples",
        indexes = {
                @Index(name = "idx_couples_user1", columnList = "user1_id"),
                @Index(name = "idx_couples_user2", columnList = "user2_id")
        })
public class Couple {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "couple_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "wedding_date")
    private LocalDate weddingDate;

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "wedding_type", length = 50)
    private String weddingType;

    @Builder.Default
    @Column(name = "honeymoon_budget", nullable = false)
    private Boolean honeymoonBudget = false;

    @Builder.Default
    @Column(name = "photo_package", nullable = false)
    private Boolean photoPackage = false;

    @Builder.Default
    @Column(name = "dress_makeup", nullable = false)
    private Boolean dressMakeup = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    // 커플 총 자산 업데이트
    public void updateTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}