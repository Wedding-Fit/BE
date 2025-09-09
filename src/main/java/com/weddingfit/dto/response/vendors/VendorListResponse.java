package com.weddingfit.dto.response.vendors;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "웨딩업체 목록 응답 DTO")
public class VendorListResponse {
    
    @Schema(description = "업체 목록")
    private List<VendorSummaryResponse> vendorList;
    
    public static VendorListResponse from(List<VendorSummaryResponse> vendorList) {
        return VendorListResponse.builder()
                .vendorList(vendorList)
                .build();
    }
}