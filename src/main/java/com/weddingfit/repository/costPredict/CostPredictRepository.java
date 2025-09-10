package com.weddingfit.repository.costPredict;

import com.weddingfit.entity.costPredict.CostPredict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CostPredictRepository extends JpaRepository<CostPredict, Long> {

    // 최신 1건: predictedAt 기준
    Optional<CostPredict> findTopByCoupleIdOrderByPredictedAtDesc(Long coupleId);

    // 보조(예비): predictedAt이 NULL이거나 동일타임스탬프일 때 PK로 최신 1건
    Optional<CostPredict> findTopByCoupleIdOrderByPredictionIdDesc(Long coupleId);

    // 히스토리 페이지 조회 (최신순)
    Page<CostPredict> findByCoupleIdOrderByPredictedAtDesc(Long coupleId, Pageable pageable);

    // 단순 일괄 삭제 (현재 서비스에서 사용 중)
    void deleteByCoupleId(Long coupleId);

    // JPQL 강제 삭제 + 즉시 flush (필요 시 사용)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CostPredict c where c.coupleId = :coupleId")
    int hardDeleteAllByCoupleId(@Param("coupleId") Long coupleId);
}
