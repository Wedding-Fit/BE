package com.weddingfit.service.community;

import com.weddingfit.entity.community.Community;
import com.weddingfit.entity.community.CommunityLike;
import com.weddingfit.entity.user.User;
import com.weddingfit.repository.community.CommunityLikeRepository;
import com.weddingfit.repository.community.CommunityRepository;
import com.weddingfit.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityLikeService {

    private final CommunityRepository communityRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final UserRepository userRepository;

    public long count(Long postId) {
        return communityLikeRepository.countByCommunity_Id(postId);
    }

    public Long findMyLikeId(Long postId, Long userId) {
        if (userId == null) return null; // 비로그인/식별 불가 시 null
        return communityLikeRepository.findByCommunity_IdAndUser_Id(postId, userId)
                .map(CommunityLike::getId)
                .orElse(null);
    }

    @Transactional
    public void like(Long postId, Long userId) {
        if (userId == null) throw new IllegalArgumentException("유저 식별 정보가 필요합니다.");

        Community post = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        boolean already = communityLikeRepository.existsByCommunity_IdAndUser_Id(postId, userId);
        if (already) {
            throw new IllegalArgumentException("이미 좋아요한 게시글입니다.");
        }

        CommunityLike like = CommunityLike.builder()
                .community(post)
                .user(user)
                .build();
        communityLikeRepository.save(like);
    }

    @Transactional
    public void unlike(Long postId, Long userId) {
        if (userId == null) throw new IllegalArgumentException("유저 식별 정보가 필요합니다.");

        CommunityLike like = communityLikeRepository.findByCommunity_IdAndUser_Id(postId, userId)
                .orElseThrow(() -> new IllegalArgumentException("좋아요 내역이 없습니다."));
        communityLikeRepository.delete(like);
    }
}
