package com.weddingfit.entity.policies;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "government_policies")
@Schema(description = "정부 정책 정보 엔티티")
public class GovernmentPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    @Schema(description = "정책 ID", example = "1")
    private Long policyId;

    @Column(name = "policy_name", nullable = false, length = 200)
    @Schema(description = "정책명", example = "신혼부부 특별공급")
    private String policyName;

    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "정책 설명", example = "신혼부부를 대상으로 한 주택 특별공급 제도입니다.")
    private String description;

    public enum Category {
        HOUSING, MARRIAGE, YOUTH, TAX
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, columnDefinition = "ENUM('HOUSING', 'MARRIAGE', 'YOUTH', 'TAX')")
    @Schema(description = "정책 카테고리", example = "MARRIAGE", allowableValues = {"HOUSING", "MARRIAGE", "YOUTH", "TAX"})
    private Category category;

    @Column(name = "website_url", length = 500)
    @Schema(description = "정책 웹사이트 URL", example = "https://www.gov.kr/policy/marriage")
    private String websiteUrl;

    @Column(name = "created_at", updatable = false)
    @Schema(description = "정책 생성일시", example = "2025-09-09T10:15:30")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}