package com.weddingfit.service.community;

import com.weddingfit.entity.community.Community;
import com.weddingfit.entity.community.CommunityLike;
import com.weddingfit.entity.user.User;
import com.weddingfit.repository.community.CommunityLikeRepository;
import com.weddingfit.repository.community.CommunityRepository;
import com.weddingfit.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityLikeServiceTest {

    @Mock CommunityRepository communityRepository;
    @Mock CommunityLikeRepository communityLikeRepository;
    @Mock UserRepository userRepository;

    CommunityLikeService likeService;

    Long postId;
    Long userId;
    Community post;
    User user;

    @BeforeEach
    void setUp() {
        likeService = new CommunityLikeService(
                communityRepository, communityLikeRepository, userRepository
        );

        postId = 10L;
        userId = 99L;

        user = User.builder()
                .id(userId)
                .nickname("테스터")
                .build();

        post = Community.builder()
                .id(postId)
                .title("제목")
                .content("내용")
                .category(Community.Category.WEDDING)
                .user(user)
                .build();
    }


    @Test
    void count_정상동작() {
        when(communityLikeRepository.countByCommunity_Id(postId)).thenReturn(7L);

        long cnt = likeService.count(postId);

        assertThat(cnt).isEqualTo(7L);
        verify(communityLikeRepository).countByCommunity_Id(postId);
    }

    @Test
    void findMyLikeId_있으면_ID반환() {
        CommunityLike like = CommunityLike.builder()
                .id(555L)
                .community(post)
                .user(user)
                .build();
        when(communityLikeRepository.findByCommunity_IdAndUser_Id(postId, userId))
                .thenReturn(Optional.of(like));

        Long myLikeId = likeService.findMyLikeId(postId, userId);

        assertThat(myLikeId).isEqualTo(555L);
    }

    @Test
    void findMyLikeId_없으면_null() {
        when(communityLikeRepository.findByCommunity_IdAndUser_Id(postId, userId))
                .thenReturn(Optional.empty());

        Long myLikeId = likeService.findMyLikeId(postId, userId);

        assertThat(myLikeId).isNull();
    }

    @Test
    void findMyLikeId_userId_null이면_null() {
        Long myLikeId = likeService.findMyLikeId(postId, null);
        assertThat(myLikeId).isNull();
        verifyNoInteractions(communityLikeRepository);
    }


    @Test
    void like_성공() {
        when(communityRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(communityLikeRepository.existsByCommunity_IdAndUser_Id(postId, userId)).thenReturn(false);

        likeService.like(postId, userId);

        ArgumentCaptor<CommunityLike> captor = ArgumentCaptor.forClass(CommunityLike.class);
        verify(communityLikeRepository).save(captor.capture());
        CommunityLike saved = captor.getValue();

        assertThat(saved.getCommunity().getId()).isEqualTo(postId);
        assertThat(saved.getUser().getId()).isEqualTo(userId);
    }

    @Test
    void like_userId_null_예외() {
        assertThatThrownBy(() -> likeService.like(postId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유저 식별 정보");
        verifyNoInteractions(communityRepository, userRepository, communityLikeRepository);
    }

    @Test
    void like_이미좋아요_예외() {
        when(communityRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(communityLikeRepository.existsByCommunity_IdAndUser_Id(postId, userId)).thenReturn(true);

        assertThatThrownBy(() -> likeService.like(postId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 좋아요한 게시글");
        verify(communityLikeRepository, never()).save(any());
    }

    @Test
    void like_게시글없음_예외() {
        when(communityRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.like(postId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 게시글");

        verifyNoInteractions(userRepository, communityLikeRepository);
    }

    @Test
    void like_사용자없음_예외() {
        when(communityRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> likeService.like(postId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자");
    }


    @Test
    void unlike_성공() {
        CommunityLike like = CommunityLike.builder()
                .id(444L)
                .community(post)
                .user(user)
                .build();
        when(communityLikeRepository.findByCommunity_IdAndUser_Id(postId, userId))
                .thenReturn(Optional.of(like));

        likeService.unlike(postId, userId);

        verify(communityLikeRepository).delete(like);
    }

    @Test
    void unlike_userId_null_예외() {
        assertThatThrownBy(() -> likeService.unlike(postId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유저 식별 정보");
        verifyNoInteractions(communityLikeRepository);
    }

    @Test
    void unlike_좋아요내역없음_예외() {
        when(communityLikeRepository.findByCommunity_IdAndUser_Id(postId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.unlike(postId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("좋아요 내역이 없습니다");
    }
}
