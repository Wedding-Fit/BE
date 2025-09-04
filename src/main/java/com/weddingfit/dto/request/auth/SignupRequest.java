package com.weddingfit.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @Schema(description = "로그인 ID", example = "greentea123")
    @NotBlank
    @Size(min=5, max=50)
    private String loginId;

    @Schema(description = "비밀번호", example = "mypassword123!")
    @NotBlank
    @Size(min=8, max = 255)
    private String password;

    @Schema(description = "실명", example = "김보성")
    @NotBlank
    @Size(max = 50)
    private String name;

    @Schema(description = "닉네임", example = "그린티")
    @Size(max = 50)
    private String nickname;

    @Schema(description = "생년월일 (yyyy-MM-dd)", example = "1999-08-30")
    private String birth;

    @Schema(description = "성별", example = "MALE")
    private String gender;

    @Schema(description = "전화번호", example = "010-1234-5678")
    @NotBlank
    @Pattern(regexp = "^(010)-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;
}
