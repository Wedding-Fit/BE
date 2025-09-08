package com.weddingfit.controller.couple;

import com.weddingfit.dto.request.couple.CoupleRegisterRequest;
import com.weddingfit.dto.response.couple.CoupleRegisterResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.global.security.JwtProvider;
import com.weddingfit.service.couple.CoupleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/couple")
@RequiredArgsConstructor
@Tag(name = "Couple", description = "커플 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class CoupleController {
    
    private final CoupleService coupleService;
    private final JwtProvider jwtProvider;

    @PostMapping("/register")
    @Operation(
            summary = "커플 등록",
            description = "현재 사용자와 상대방 사용자를 커플로 등록합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "커플 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "409", description = "이미 커플 등록됨")
    })
    public BaseResponse<CoupleRegisterResponse> registerCouple(
            @Valid @RequestBody CoupleRegisterRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        String token = extractTokenFromHeader(authorization);
        Long userId = jwtProvider.getUserId(token);
        
        CoupleRegisterResponse response = coupleService.registerCouple(request, userId);
        
        return BaseResponse.success(response, "커플 등록에 성공했습니다");
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