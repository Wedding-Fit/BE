package com.weddingfit.dto.response.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CommentResponse {
    @Schema(description = "댓글 개수", example = "1")
    private long commentCount;
    private List<Item> commentList;

    @Getter
    @AllArgsConstructor
    public static class Item {
        @Schema(description = "작성자 닉네임", example = "보성")
        private String nickname;
        @Schema(description = "댓글 내용", example = "내용")
        private String content;
    }
}
