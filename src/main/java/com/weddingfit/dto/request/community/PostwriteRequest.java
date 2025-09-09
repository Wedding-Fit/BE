package com.weddingfit.dto.request.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostwriteRequest {
    @Schema(description = "제목", example = "제목")
    private String title;
    @Schema(description = "작성자 ID", example = "1")
    private Long userId;
    @Schema(description = "카테고리", example = "결혼")
    private String category;
    @Schema(description = "내용", example = "내용")
    private String content;
}
