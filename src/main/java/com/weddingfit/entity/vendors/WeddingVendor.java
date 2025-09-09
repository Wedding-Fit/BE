package com.weddingfit.entity.vendors;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "wedding_vendors")
@Schema(description = "웨딩 업체 정보 엔티티")
public class WeddingVendor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_id")
    @Schema(description = "업체 ID", example = "1")
    private Long vendorId;
    
    @Column(name = "vendor_name", nullable = false, length = 100)
    @Schema(description = "업체명", example = "더리버사이드 호텔 콘서트홀")
    private String vendorName;
    
    public enum VendorType {
        RENTAL, PERSON_MEAL, FLOWER, GUESTS, CEREMONY, DINING
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_type", nullable = false, columnDefinition = "ENUM('RENTAL', 'PERSON_MEAL', 'FLOWER', 'GUESTS', 'CEREMONY', 'DINING')")
    @Schema(description = "업체 유형", example = "RENTAL")
    private VendorType vendorType;
    
    @Column(name = "region", length = 50)
    @Schema(description = "지역", example = "서울특별시")
    private String region;
    
    @Column(name = "rating", precision = 3, scale = 2)
    @Schema(description = "평점", example = "4.5")
    private BigDecimal rating;
    
    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "업체 설명")
    private String description;
    
    @Column(name = "address", length = 200)
    @Schema(description = "주소", example = "서울 서초구 강남대로 107길 8")
    private String address;
    
    @Column(name = "subway_access", length = 100)
    @Schema(description = "지하철 접근성", example = "3호선 신사역 3번 출구 도보 3분")
    private String subwayAccess;
    
    @Column(name = "parking_info", length = 200)
    @Schema(description = "주차 정보", example = "내부 60대, 제휴 주차장 보유")
    private String parkingInfo;
    
    @Column(name = "opening_hours", length = 100)
    @Schema(description = "운영시간", example = "월~일 10:00 ~ 19:00")
    private String openingHours;
    
    @Column(name = "phone_number", length = 20)
    @Schema(description = "전화번호", example = "02-6710-1148")
    private String phoneNumber;
    
    @Column(name = "website_url", length = 500)
    @Schema(description = "웹사이트 URL")
    private String websiteUrl;
    
    @Column(name = "image_url", length = 500)
    @Schema(description = "이미지 URL")
    private String imageUrl;
    
    @Column(name = "created_at", updatable = false)
    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}