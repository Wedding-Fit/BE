package com.weddingfit.dto.response.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostdetailResponse {
    @Schema(description = "글 ID", example = "1")
    private Long postId;
    @Schema(description = "제목", example = "제목")
    private String title;
    @Schema(description = "닉네임", example = "그린티")
    private String nickname;
    @Schema(description = "카테고리", example = "결혼")
    private String category;
    @Schema(description = "내용", example = "내용")
    private String content;
    @Schema(description = "좋아요", example = "1")
    private long likeCount;
    @Schema(description = "작성 날짜", example = "2025.09.09")
    private String createdAt;
    @Schema(description = "좋아요 여부", example = "1")
    private Long likeId;
}
