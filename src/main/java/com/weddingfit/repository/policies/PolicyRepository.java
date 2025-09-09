package com.weddingfit.repository.policies;

import com.weddingfit.entity.policies.GovernmentPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<GovernmentPolicy, Long> {
}