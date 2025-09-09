package com.weddingfit.dto.response.vendors;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "웨딩업체 상세 항목 DTO")
public class VendorItemResponse {
    
    @Schema(description = "업체 유형", example = "RENTAL")
    private String vendorType;
    
    @Schema(description = "설명", example = "11,500,000원")
    private String description;
}