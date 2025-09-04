package com.weddingfit.controller.auth;

import com.weddingfit.dto.request.auth.SignupRequest;
import com.weddingfit.dto.response.auth.SignupResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원 인증 관련 API")
public class AuthController {
    private final AuthService authService;

    @PostMapping("join")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "회원가입",
            description = "신규 사용자를 등록합니다. 비밀번호는 암호화 저장되며, 아이디/닉네임/전화번호 중복 검증을 수행합니다."
    )
    @ApiResponse(
            responseCode = "201",
            description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = SignupResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 입력값"
    )
    @ApiResponse(
            responseCode = "409",
            description = "중복된 아이디/닉네임/전화번호"
    )
    public BaseResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request){
        SignupResponse response = authService.signup(request);
        return BaseResponse.success(response, "회원가입이 완료 되었습니다.");
    }
}
