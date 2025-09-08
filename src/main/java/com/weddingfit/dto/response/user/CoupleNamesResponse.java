package com.weddingfit.dto.response.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "커플 이름 조회 응답 DTO")
public class CoupleNamesResponse {

    @Schema(description = "여성 이름", example = "최예빈")
    private String femaleName;

    @Schema(description = "남성 이름", example = "김보성")
    private String maleName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "결혼 예정일 (yyyy-MM-dd)", example = "2025-09-12")
    private LocalDate weddingDate;
}