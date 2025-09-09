package com.weddingfit.dto.response.policies;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.weddingfit.entity.policies.GovernmentPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "정부 정책 단건 응답 DTO")
public class PolicyResponse {

    @Schema(description = "정책 ID", example = "1")
    private Long policyId;

    @Schema(description = "정책명", example = "신혼부부 특별공급")
    private String policyName;

    @Schema(description = "정책 설명", example = "신혼부부를 대상으로 한 주택 특별공급 제도")
    private String description;

    @Schema(description = "정책 카테고리", example = "MARRIAGE", allowableValues = {"HOUSING", "MARRIAGE", "YOUTH", "TAX"})
    private String category;

    @Schema(description = "정책 웹사이트 URL", example = "https://www.gov.kr/policy/marriage")
    private String websiteUrl;

    @JsonFormat(pattern = "yyyy.MM.dd")
    @Schema(description = "정책 생성일자", example = "2025.09.09")
    private LocalDateTime createdAt;

    public static PolicyResponse from(GovernmentPolicy policy) {
        return PolicyResponse.builder()
                .policyId(policy.getPolicyId())
                .policyName(policy.getPolicyName())
                .description(policy.getDescription())
                .category(policy.getCategory().name())
                .websiteUrl(policy.getWebsiteUrl())
                .createdAt(policy.getCreatedAt())
                .build();
    }
}