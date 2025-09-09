package com.weddingfit.repository.deposit;

import com.weddingfit.entity.company.FinancialCompany;
import com.weddingfit.entity.deposit.*;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositSavingRepository extends JpaRepository<DepositSaving, Long> {
  Optional<DepositSaving> findByCompanyAndNameAndTypeAndSaveMonth(
      FinancialCompany company, String name, DepositSavingType type, String saveMonth);
}
