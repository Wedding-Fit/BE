package com.weddingfit.dto.response.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostwriteResponse {
    @Schema(description = "글 ID", example = "1")
    private Long postId;
}
