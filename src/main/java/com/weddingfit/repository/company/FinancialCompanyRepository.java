package com.weddingfit.repository.company;

import com.weddingfit.entity.company.FinancialCompany;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialCompanyRepository extends JpaRepository<FinancialCompany, Long> {
  Optional<FinancialCompany> findByCode(String code);
  Optional<FinancialCompany> findByName(String name);
}