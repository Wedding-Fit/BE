package com.weddingfit.repository.loan;

import com.weddingfit.entity.company.FinancialCompany;
import com.weddingfit.entity.loan.PersonalCreditLoan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalCreditLoanRepository extends JpaRepository<PersonalCreditLoan, Long> {
  Optional<PersonalCreditLoan> findByCompanyAndName(FinancialCompany company, String name);
}
