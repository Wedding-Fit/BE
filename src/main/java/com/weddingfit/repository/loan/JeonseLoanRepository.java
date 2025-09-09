package com.weddingfit.repository.loan;

import com.weddingfit.entity.company.FinancialCompany;
import com.weddingfit.entity.loan.JeonseLoan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JeonseLoanRepository extends JpaRepository<JeonseLoan, Long> {
  Optional<JeonseLoan> findByCompanyAndName(FinancialCompany company, String name);
}
