package com.weddingfit.dto.response.community;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "좋아요 성공 응답 data(빈 객체)")
public class LikeResponse {

    @Schema(hidden = true)
    private Boolean placeholder;
}
