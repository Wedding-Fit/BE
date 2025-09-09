package com.weddingfit.repository.community;

import com.weddingfit.entity.community.CommunityLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;   // ✅ 추가
import org.springframework.transaction.annotation.Transactional; // ✅ 추가

import java.util.Optional;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> {

    boolean existsByCommunity_IdAndUser_Id(Long communityId, Long userId);

    Optional<CommunityLike> findByCommunity_IdAndUser_Id(Long communityId, Long userId);

    long countByCommunity_Id(Long communityId);

    Optional<CommunityLike> findByIdAndCommunity_IdAndUser_Id(Long id, Long communityId, Long userId);

    void deleteByCommunity_IdAndUser_Id(Long communityId, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    long deleteByCommunity_Id(Long communityId);
}
