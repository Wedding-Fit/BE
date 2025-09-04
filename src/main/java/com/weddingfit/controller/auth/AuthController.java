package com.weddingfit.controller.auth;

import com.weddingfit.dto.request.auth.SignupRequest;
import com.weddingfit.dto.response.auth.SignupResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("join")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request){
        SignupResponse response = authService.signup(request);
        return BaseResponse.success(response, "회원가입이 완료 되었습니다.");
    }
}
