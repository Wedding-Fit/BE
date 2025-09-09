package com.weddingfit.repository.vendors;

import com.weddingfit.entity.vendors.WeddingVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<WeddingVendor, Long> {
}