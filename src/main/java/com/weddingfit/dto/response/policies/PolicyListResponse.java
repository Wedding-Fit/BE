package com.weddingfit.dto.response.policies;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "정부 정책 목록 응답 DTO")
public class PolicyListResponse {

    @Schema(description = "정책 리스트")
    private List<PolicyResponse> policyList;

    public static PolicyListResponse from(List<PolicyResponse> policyList) {
        return PolicyListResponse.builder()
                .policyList(policyList)
                .build();
    }
}