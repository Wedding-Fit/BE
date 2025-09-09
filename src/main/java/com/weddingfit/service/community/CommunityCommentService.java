package com.weddingfit.service.community;

import com.weddingfit.dto.response.community.CommentResponse;
import com.weddingfit.entity.community.Comment;
import com.weddingfit.entity.user.User;
import com.weddingfit.repository.community.CommunityCommentRepository;
import com.weddingfit.repository.community.CommunityRepository;
import com.weddingfit.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityCommentService {

    private final CommunityRepository communityRepository;
    private final CommunityCommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CommentResponse getPostComments(Long postId) {
        communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        List<Comment> comments = commentRepository.findByCommunity_IdOrderByCreatedAtAsc(postId);

        List<CommentResponse.Item> items = comments.stream()
                .map(c -> new CommentResponse.Item(
                        c.getUser().getNickname(),
                        c.getContent()
                ))
                .toList();

        return new CommentResponse(items.size(), items);
    }

    @Transactional
    public Long createComment(Long postId, Long userId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력하세요.");
        }

        var community = communityRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        Comment comment = Comment.builder()
                .community(community)
                .user(user)
                .content(content)
                .build();

        Comment saved = commentRepository.save(comment);
        return saved.getId();
    }
}