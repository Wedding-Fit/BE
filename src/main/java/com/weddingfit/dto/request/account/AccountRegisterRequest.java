package com.weddingfit.dto.request.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AccountRegisterRequest", description = "계좌 등록 요청 DTO")
public class AccountRegisterRequest {

    @NotBlank(message = "은행명은 필수입니다")
    @Schema(description = "은행명", example = "KB국민은행", required = true)
    private String bankName;

    @NotBlank(message = "인터넷뱅킹 아이디는 필수입니다")
    @Schema(description = "인터넷뱅킹 로그인 ID", example = "user1234", required = true)
    private String bankId;

    @NotBlank(message = "인터넷뱅킹 비밀번호는 필수입니다")
    @Schema(
            description = "인터넷뱅킹 로그인 비밀번호 (민감정보, writeOnly 권장)",
            example = "p@ssw0rd!",
            required = true,
            accessMode = Schema.AccessMode.WRITE_ONLY
    )
    private String bankPassword;

    @NotBlank(message = "계좌번호는 필수입니다")
    @Schema(description = "계좌번호", example = "110-123-456789", required = true)
    private String accountNumber;

    @Schema(
            description = "계좌 비밀번호 (선택적, 숫자 4자리 등. 민감정보)",
            example = "1234",
            required = false,
            accessMode = Schema.AccessMode.WRITE_ONLY
    )
    private String accountPassword;
}