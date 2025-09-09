package com.weddingfit.entity.vendors;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vendor_items")
@Schema(description = "웨딩업체 상세 항목 엔티티")
public class VendorItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private WeddingVendor vendor;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_type", nullable = false)
    private WeddingVendor.VendorType vendorType;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}