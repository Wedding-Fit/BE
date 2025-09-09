package com.weddingfit.repository.loan;

import com.weddingfit.entity.company.FinancialCompany;
import com.weddingfit.entity.loan.PersonalCreditLoan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PersonalCreditLoanRepository extends JpaRepository<PersonalCreditLoan, Long> {
  Optional<PersonalCreditLoan> findByCompanyAndName(FinancialCompany company, String name);

  Page<PersonalCreditLoan> findAll(Pageable pageable);

  @Query("select p from PersonalCreditLoan p join fetch p.company where p.id = :id")
  Optional<PersonalCreditLoan> findByIdFetchCompany(@Param("id") Long id);
}
