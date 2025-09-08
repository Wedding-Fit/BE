package com.weddingfit.controller.user;

import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.global.security.JwtProvider;
import com.weddingfit.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    private final JwtProvider jwtProvider;
    
    @DeleteMapping("/me")
    @Operation(
            summary = "회원 탈퇴",
            description = "인증된 사용자의 계정을 비활성화합니다. 관련된 리프레시 토큰도 모두 삭제됩니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "회원 탈퇴 성공"
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증이 필요합니다"
    )
    @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없습니다"
    )
    public BaseResponse<Void> deleteMe(@RequestHeader(value = "Authorization", required = false) String authorization) {
        // Authorization 헤더에서 토큰 추출
        String token = extractTokenFromHeader(authorization);
        
        // JWT에서 사용자 ID 추출
        Long userId = jwtProvider.getUserId(token);
        
        // 회원 탈퇴 처리
        userService.deleteUser(userId);
        
        return BaseResponse.success(null, "회원 탈퇴가 완료되었습니다");
    }
    
    private String extractTokenFromHeader(String authorization) {
        if (authorization == null || authorization.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization header is required");
        }
        if (!authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header format. Use 'Bearer <token>'");
        }
        return authorization.substring(7); // "Bearer " 제거
    }
}