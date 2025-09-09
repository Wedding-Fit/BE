package com.weddingfit.dto.request.community;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentwriteRequest {
    @NotBlank(message = "content는 필수입니다.")
    private String content;
}
