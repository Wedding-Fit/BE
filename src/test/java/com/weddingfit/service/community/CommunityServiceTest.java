package com.weddingfit.service.community;

import com.weddingfit.dto.request.community.PostlistRequest;
import com.weddingfit.dto.request.community.PostwriteRequest;
import com.weddingfit.dto.response.community.PostdetailResponse;
import com.weddingfit.entity.community.Community;
import com.weddingfit.entity.user.User;
import com.weddingfit.repository.community.CommunityCommentRepository;
import com.weddingfit.repository.community.CommunityLikeRepository;
import com.weddingfit.repository.community.CommunityRepository;
import com.weddingfit.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @Mock CommunityRepository communityRepository;
    @Mock UserRepository userRepository;
    @Mock CommunityLikeService communityLikeService;
    @Mock CommunityCommentRepository communityCommentRepository;
    @Mock CommunityLikeRepository communityLikeRepository;

    @InjectMocks CommunityService communityService;

    Long userId;
    Long postId;
    User user;
    Community post;

    @BeforeEach
    void init() {
        userId = 1L;
        postId = 100L;

        user = User.builder().id(userId).nickname("테스터").build();
        post = Community.builder()
                .id(postId)
                .title("테스트제목")
                .content("테스트내용")
                .category(Community.Category.WEDDING)
                .user(user)
                .likeCount(5)
                .createdAt(LocalDateTime.of(2025, 9, 1, 10, 0))
                .build();
    }

    @Test
    void 게시글_생성_성공() {
        PostwriteRequest req = PostwriteRequest.builder()
                .title("타이틀")
                .content("내용")
                .userId(userId)
                .category("결혼")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(communityRepository.save(any())).thenReturn(post);

        var res = communityService.createPost(req, userId, Community.Category.WEDDING);

        assertThat(res.getPostId()).isEqualTo(postId);
        verify(communityRepository).save(any(Community.class));
    }

    @Test
    void 카테고리별_조회() {
        when(communityRepository.findByCategory(Community.Category.WEDDING))
                .thenReturn(List.of(post));

        var list = communityService.getPostsByCategory(PostlistRequest.Category.WEDDING);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(postId);
    }

    @Test
    void 게시글_상세조회_좋아요포함() {
        when(communityRepository.findById(postId)).thenReturn(Optional.of(post));
        when(communityLikeService.findMyLikeId(postId, userId)).thenReturn(123L);

        PostdetailResponse detail = communityService.getPostDetail(postId, userId);

        assertThat(detail.getPostId()).isEqualTo(postId);
        assertThat(detail.getTitle()).isEqualTo("테스트제목");
        assertThat(detail.getNickname()).isEqualTo("테스터");
        assertThat(detail.getCategory()).isEqualTo("결혼");
        assertThat(detail.getLikeCount()).isEqualTo(5);
        assertThat(detail.getLikeId()).isEqualTo(123L);
        assertThat(detail.getCreatedAt()).isEqualTo("2025.09.01");
    }

    @Test
    void 게시글_삭제_성공() {
        when(communityRepository.findById(postId)).thenReturn(Optional.of(post));

        communityService.deletePost(postId, userId);

        verify(communityCommentRepository).deleteByCommunity_Id(postId);
        verify(communityLikeRepository).deleteByCommunity_Id(postId);
        verify(communityRepository).delete(post);
    }

    @Test
    void 게시글_삭제_작성자아님_예외() {
        when(communityRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> communityService.deletePost(postId, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("삭제 권한");
    }
}
