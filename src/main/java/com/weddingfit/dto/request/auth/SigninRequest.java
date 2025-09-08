package com.weddingfit.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SigninRequest {
    @Schema(description = "로그인 ID", example = "test1234")
    @NotBlank
    private String loginId;

    @Schema(description = "비밀번호", example = "1234")
    @NotBlank
    private String password;
}