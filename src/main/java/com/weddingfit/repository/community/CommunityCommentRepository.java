package com.weddingfit.repository.community;

import com.weddingfit.entity.community.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByCommunity_IdOrderByCreatedAtAsc(Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    long deleteByCommunity_Id(Long postId);
}
