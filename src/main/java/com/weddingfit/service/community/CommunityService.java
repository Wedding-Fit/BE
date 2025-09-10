package com.weddingfit.service.community;

import com.weddingfit.dto.request.community.PostlistRequest;
import com.weddingfit.dto.request.community.PostwriteRequest;
import com.weddingfit.dto.response.community.PostdetailResponse;
import com.weddingfit.dto.response.community.PostlistResponse;
import com.weddingfit.dto.response.community.PostwriteResponse;
import com.weddingfit.entity.community.Community;
import com.weddingfit.entity.user.User;
import com.weddingfit.repository.community.CommunityCommentRepository;
import com.weddingfit.repository.community.CommunityLikeRepository;
import com.weddingfit.repository.community.CommunityRepository;
import com.weddingfit.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    private final CommunityLikeService communityLikeService;

    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityLikeRepository communityLikeRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Transactional
    public PostwriteResponse createPost(PostwriteRequest request, Long userId, Community.Category category) {
        if (userId == null) {
            throw new IllegalArgumentException("인증된 사용자 정보가 필요합니다.");
        }
        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Community post = Community.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(category)
                .user(user)
                .build();

        Community saved = communityRepository.save(post);
        return new PostwriteResponse(saved.getId());
    }

    @Transactional(readOnly = true)
    public List<Community> getPostsByCategory(PostlistRequest.Category category) {
        return communityRepository.findByCategory(
                Community.Category.valueOf(category.name())
        );
    }

    @Transactional(readOnly = true)
    public PostlistResponse.Data getPostListDataByCategory(PostlistRequest.Category category) {
        List<Community> posts = getPostsByCategory(category);
        if (posts.isEmpty()) {
            return PostlistResponse.Data.fromEntitiesWithLikes(Collections.emptyList(), Collections.emptyMap());
        }

        Map<Long, Long> likeMap = new HashMap<>();
        for (Community p : posts) {
            long cnt = p.getLikeCount() != null ? p.getLikeCount().longValue() : 0L;
            likeMap.put(p.getId(), cnt);
        }

        return PostlistResponse.Data.fromEntitiesWithLikes(posts, likeMap);
    }

    @Transactional(readOnly = true)
    public PostdetailResponse getPostDetail(Long postId, Long currentUserId) {
        Community post = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        long likeCount = post.getLikeCount() != null ? post.getLikeCount().longValue() : 0L;
        Long likeId = communityLikeService.findMyLikeId(postId, currentUserId);

        String nickname = Optional.ofNullable(post.getUser())
                .map(User::getNickname)
                .orElse("익명");

        String createdAt = Optional.ofNullable(post.getCreatedAt())
                .map(t -> t.format(DATE_FMT))
                .orElse(null);

        return new PostdetailResponse(
                post.getId(),
                post.getTitle(),
                nickname,
                toKoreanCategory(post.getCategory()),
                post.getContent(),
                likeCount,
                createdAt,
                likeId
        );
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        if (userId == null) throw new IllegalArgumentException("사용자 ID가 필요합니다.");

        Community post = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Long ownerId = Optional.ofNullable(post.getUser())
                .map(User::getId)
                .orElse(null);

        if (ownerId == null || !ownerId.equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        communityCommentRepository.deleteByCommunity_Id(postId);
        communityLikeRepository.deleteByCommunity_Id(postId);

        communityRepository.delete(post);
    }

    private Community.Category mapCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
        String s = raw.trim();
        switch (s) {
            case "결혼": return Community.Category.WEDDING;
            case "저축": return Community.Category.SAVING;
            case "주거": return Community.Category.HOUSING;
            case "세금": return Community.Category.TAX;
            case "지출": return Community.Category.USED;
            case "조언":   return Community.Category.TIPS;
            case "기타": return Community.Category.ETC;
            default:
                try {
                    return Community.Category.valueOf(s.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + raw);
                }
        }
    }

    private String toKoreanCategory(Community.Category category) {
        if (category == null) return null;
        switch (category) {
            case WEDDING: return "결혼";
            case SAVING:  return "저축";
            case HOUSING: return "주택";
            case TAX:     return "세금";
            case USED:    return "중고";
            case TIPS:    return "팁";
            case ETC:     return "기타";
            default:      return category.name();
        }
    }
    
    /**
     * 모든 게시글의 likeCount를 실제 좋아요 수와 동기화
     * (기존 데이터 마이그레이션용)
     */
    @Transactional
    public void syncAllLikeCounts() {
        List<Community> allPosts = communityRepository.findAll();
        for (Community post : allPosts) {
            long actualLikeCount = communityLikeRepository.countByCommunity_Id(post.getId());
            if (post.getLikeCount() == null || post.getLikeCount() != actualLikeCount) {
                post.setLikeCount((int) actualLikeCount);
                communityRepository.save(post);
            }
        }
    }
}
