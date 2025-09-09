package com.weddingfit.controller.policies;

import com.weddingfit.dto.response.policies.PolicyListResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.global.security.JwtProvider;
import com.weddingfit.service.policies.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Policy", description = "정부 정책 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class PolicyController {
    
    private final PolicyService policyService;
    private final JwtProvider jwtProvider;
    
    @GetMapping
    @Operation(
            summary = "정부 정책 목록 조회",
            description = "모든 정부 정책 목록을 조회합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정책 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public BaseResponse<PolicyListResponse> getAllPolicies(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        String token = extractTokenFromHeader(authorization);
        jwtProvider.getUserId(token);
        
        PolicyListResponse response = policyService.getAllPolicies();
        
        return BaseResponse.success(response, "성공했습니다");
    }
    
    private String extractTokenFromHeader(String authorization) {
        if (authorization == null || authorization.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization header is required");
        }
        if (!authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header format. Use 'Bearer <token>'");
        }
        return authorization.substring(7);
    }
}