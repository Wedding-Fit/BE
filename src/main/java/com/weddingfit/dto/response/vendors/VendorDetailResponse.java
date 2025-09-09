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
@Schema(description = "웨딩업체 상세 조회 응답 DTO")
public class VendorDetailResponse {
    
    @Schema(description = "업체 ID", example = "101")
    private Long vendorId;
    
    @Schema(description = "업체명", example = "더리버사이드 호텔 • 콘서트홀")
    private String vendorName;
    
    @Schema(description = "업체 항목 리스트")
    private List<VendorItemResponse> items;
    
    @Schema(description = "주소", example = "서울 서초구 강남대로 107길 8(잠원동), 더리버사이드호텔 콘서트홀")
    private String address;
    
    @Schema(description = "지하철 접근성", example = "3호선 신사역 3번 출구 도보 3분")
    private String subwayAccess;
    
    @Schema(description = "주차 정보", example = "내부 60대, 제휴 주차장 보유 (식권 제공)")
    private String parkingInfo;
    
    @Schema(description = "운영시간", example = "목~일 10:00 ~ 19:00")
    private String openingHours;
    
    @Schema(description = "전화번호", example = "02-6710-1148")
    private String phoneNumber;
    
    @Schema(description = "웹사이트 URL", example = "https://www.riversidehotel.co.kr/pages/wed1.php")
    private String websiteUrl;
    
    @Schema(description = "이미지 URL", example = "https://cdn.example.com/vendors/101.jpg")
    private String imageUrl;
}