package com.weddingfit.dto.response.couple;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoupleRegisterResponse {
    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;
}