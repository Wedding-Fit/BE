package com.weddingfit.repository.loan;

import com.weddingfit.entity.company.FinancialCompany;
import com.weddingfit.entity.loan.JeonseLoan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface JeonseLoanRepository extends JpaRepository<JeonseLoan, Long> {
  Optional<JeonseLoan> findByCompanyAndName(FinancialCompany company, String name);

  Page<JeonseLoan> findAll(Pageable pageable);

    @Query("select j from JeonseLoan j join fetch j.company where j.id = :id")
    Optional<JeonseLoan> findByIdFetchCompany(@Param("id") Long id);
}
