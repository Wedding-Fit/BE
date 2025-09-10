package com.weddingfit.repository.deposit;

import com.weddingfit.entity.company.FinancialCompany;
import com.weddingfit.entity.deposit.*;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.*;
import java.util.*;

@Repository
public interface DepositSavingRepository extends JpaRepository<DepositSaving, Long> {
  Optional<DepositSaving> findByCompanyAndNameAndTypeAndSaveMonth(
      FinancialCompany company, String name, DepositSavingType type, String saveMonth);

  @EntityGraph(attributePaths = "company")
    Page<DepositSaving> findByType(DepositSavingType type, Pageable pageable);

   @Query("select ds from DepositSaving ds join fetch ds.company where ds.id = :id")
    Optional<DepositSaving> findByIdFetchCompany(@Param("id") Long id);

    // 타입에 따라 이자율 내림차순 Top 5
    List<DepositSaving> findTop5ByTypeOrderByInterestRateDesc(DepositSavingType type);
    // 목표까지 남은 개월 수 이하인 상품 중 이자율 높은 순으로 정렬
    List<DepositSaving> findTop20ByTypeOrderByInterestRateDesc(DepositSavingType type);

    // 예금 상품 상위 10개 (금리순)
    List<DepositSaving> findTop10ByTypeOrderByInterestRateDesc(DepositSavingType type);
}
