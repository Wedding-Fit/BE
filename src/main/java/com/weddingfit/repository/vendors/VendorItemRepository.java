package com.weddingfit.repository.vendors;

import com.weddingfit.entity.vendors.VendorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorItemRepository extends JpaRepository<VendorItem, Long> {
    
    @Query("SELECT vi FROM VendorItem vi WHERE vi.vendor.vendorId = :vendorId")
    List<VendorItem> findByVendorId(@Param("vendorId") Long vendorId);
}