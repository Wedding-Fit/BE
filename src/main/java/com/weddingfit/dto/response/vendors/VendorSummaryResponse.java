package com.weddingfit.dto.response.vendors;

import com.weddingfit.entity.vendors.WeddingVendor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "웨딩업체 목록 조회 응답 DTO")
public class VendorSummaryResponse {
    
    @Schema(description = "업체 ID", example = "1")
    private Long vendorId;
    
    @Schema(description = "업체명", example = "더리버사이드 호텔 콘서트홀")
    private String vendorName;
    
    @Schema(description = "지역", example = "서울특별시")
    private String region;
    
    @Schema(description = "평점", example = "3.0")
    private BigDecimal rating;
    
    @Schema(description = "이미지 URL", example = "https://cdn.example.com/vendors/101.jpg")
    private String imageUrl;
    
    public static VendorSummaryResponse from(WeddingVendor vendor) {
        return VendorSummaryResponse.builder()
                .vendorId(vendor.getVendorId())
                .vendorName(vendor.getVendorName())
                .region(vendor.getRegion())
                .rating(vendor.getRating())
                .imageUrl(vendor.getImageUrl())
                .build();
    }
}