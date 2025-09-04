package com.weddingfit.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SignupResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "로그인 ID", example = "greentea123")
    private String loginId;

    @Schema(description = "실명", example = "김보성")
    private String name;

    @Schema(description = "닉네임", example = "그린티")
    private String nickname;

    @Schema(description = "생성 일시", example = "2025-09-04T21:35:00")
    private LocalDateTime createdAt;
}
