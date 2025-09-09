package com.weddingfit.service.vendors;

import com.weddingfit.dto.response.vendors.*;
import com.weddingfit.entity.vendors.WeddingVendor;
import com.weddingfit.global.exception.CustomException;
import com.weddingfit.global.exception.GlobalErrorCode;
import com.weddingfit.repository.vendors.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VendorService {
    
    private final VendorRepository vendorRepository;
    
    public VendorListResponse getAllVendors() {
        List<WeddingVendor> vendors = vendorRepository.findAll();
        
        List<VendorSummaryResponse> vendorResponses = vendors.stream()
                .map(VendorSummaryResponse::from)
                .collect(Collectors.toList());
        
        return VendorListResponse.from(vendorResponses);
    }
    
    public VendorDetailResponse getVendorDetail(Long vendorId) {
        WeddingVendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND));
        
        // description을 파싱해서 items 생성
        List<VendorItemResponse> itemResponses = parseDescriptionToItems(vendor.getDescription());
        
        return VendorDetailResponse.builder()
                .vendorId(vendor.getVendorId())
                .vendorName(vendor.getVendorName())
                .items(itemResponses)
                .address(vendor.getAddress())
                .subwayAccess(vendor.getSubwayAccess())
                .parkingInfo(vendor.getParkingInfo())
                .openingHours(vendor.getOpeningHours())
                .phoneNumber(vendor.getPhoneNumber())
                .websiteUrl(vendor.getWebsiteUrl())
                .imageUrl(vendor.getImageUrl())
                .build();
    }
    
    private List<VendorItemResponse> parseDescriptionToItems(String description) {
        if (description == null || description.trim().isEmpty()) {
            return List.of();
        }
        
        List<VendorItemResponse> items = new java.util.ArrayList<>();
        String[] parts = description.split(" \\| ");
        
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("대관료 ")) {
                items.add(VendorItemResponse.builder()
                        .vendorType("RENTAL")
                        .description(part.substring(3))
                        .build());
            } else if (part.contains("식대 ")) {
                items.add(VendorItemResponse.builder()
                        .vendorType("PERSON_MEAL")
                        .description(part)
                        .build());
            } else if (part.startsWith("꽃장식 ") || part.contains("꽃장식")) {
                items.add(VendorItemResponse.builder()
                        .vendorType("FLOWER")
                        .description(part.contains("꽃장식 ") ? part.substring(part.indexOf("꽃장식 ") + 3) : part)
                        .build());
            } else if (part.contains("명") && (part.contains("추천") || part.contains("보증") || part.contains("수용"))) {
                items.add(VendorItemResponse.builder()
                        .vendorType("GUESTS")
                        .description(part)
                        .build());
            } else if (part.contains("예식간격") || part.contains("예식 간격")) {
                items.add(VendorItemResponse.builder()
                        .vendorType("CEREMONY")
                        .description(part)
                        .build());
            } else if (part.equals("뷔페") || part.equals("코스요리") || part.contains("식사")) {
                items.add(VendorItemResponse.builder()
                        .vendorType("DINING")
                        .description(part)
                        .build());
            }
        }
        
        return items;
    }
}