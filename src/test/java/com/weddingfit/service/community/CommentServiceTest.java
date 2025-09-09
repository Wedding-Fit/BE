package com.weddingfit.service.community;

import com.weddingfit.dto.response.community.CommentResponse;
import com.weddingfit.entity.community.Comment;
import com.weddingfit.entity.community.Community;
import com.weddingfit.entity.user.User;
import com.weddingfit.repository.community.CommunityCommentRepository;
import com.weddingfit.repository.community.CommunityRepository;
import com.weddingfit.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityCommentServiceTest {

    @Mock CommunityRepository communityRepository;
    @Mock CommunityCommentRepository commentRepository;
    @Mock UserRepository userRepository;

    CommunityCommentService commentService;

    Long postId;
    Long userId;
    Community post;
    User user;

    @BeforeEach
    void setUp() {
        commentService = new CommunityCommentService(
                communityRepository, commentRepository, userRepository
        );

        postId = 11L;
        userId = 22L;

        user = User.builder()
                .id(userId)
                .nickname("보성")
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
    void getPostComments_리스트매핑_성공() {
        when(communityRepository.findById(postId)).thenReturn(Optional.of(post));

        Comment c1 = Comment.builder()
                .id(1L)
                .community(post)
                .user(User.builder().id(100L).nickname("닉1").build())
                .content("첫 댓글")
                .build();
        Comment c2 = Comment.builder()
                .id(2L)
                .community(post)
                .user(User.builder().id(101L).nickname("닉2").build())
                .content("둘째 댓글")
                .build();

        when(commentRepository.findByCommunity_IdOrderByCreatedAtAsc(postId))
                .thenReturn(List.of(c1, c2));

        CommentResponse res = commentService.getPostComments(postId);

        assertThat(res.getCommentCount()).isEqualTo(2);
        assertThat(res.getCommentList()).extracting("nickname")
                .containsExactly("닉1", "닉2");
        assertThat(res.getCommentList()).extracting("content")
                .containsExactly("첫 댓글", "둘째 댓글");

        verify(communityRepository).findById(postId);
        verify(commentRepository).findByCommunity_IdOrderByCreatedAtAsc(postId);
    }

    @Test
    void getPostComments_게시글없음_예외() {
        when(communityRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getPostComments(postId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글이 존재하지 않습니다.");
        verify(commentRepository, never()).findByCommunity_IdOrderByCreatedAtAsc(any());
    }

    @Test
    void createComment_성공() {
        // given
        when(communityRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any())).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            return Comment.builder()
                    .id(999L)
                    .community(c.getCommunity())
                    .user(c.getUser())
                    .content(c.getContent())
                    .build();
        });

        Long savedId = commentService.createComment(postId, userId, "안녕하세요!");

        assertThat(savedId).isEqualTo(999L);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        Comment saved = captor.getValue();
        assertThat(saved.getCommunity().getId()).isEqualTo(postId);
        assertThat(saved.getUser().getId()).isEqualTo(userId);
        assertThat(saved.getContent()).isEqualTo("안녕하세요!");
    }

    @Test
    void createComment_빈내용_예외() {
        assertThatThrownBy(() -> commentService.createComment(postId, userId, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("댓글 내용을 입력하세요.");
        verifyNoInteractions(commentRepository);
    }

    @Test
    void createComment_게시글없음_예외() {
        when(communityRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(postId, userId, "안녕"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글이 존재하지 않습니다.");
        verify(userRepository, never()).findById(any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_사용자없음_예외() {
        when(communityRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(postId, userId, "안녕"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자가 존재하지 않습니다.");
        verify(commentRepository, never()).save(any());
    }
}
