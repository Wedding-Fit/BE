package com.weddingfit.dto.response.community;

import com.weddingfit.entity.community.Community;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class PostlistResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {
        private List<Board> boardList;

        public static Data fromEntities(List<Community> posts) {
            return Data.builder()
                    .boardList(posts.stream().map(Board::fromEntity).collect(toList()))
                    .build();
        }

        public static Data fromEntitiesWithLikes(List<Community> posts, Map<Long, Long> likeCountMap) {
            return Data.builder()
                    .boardList(posts.stream()
                            .map(p -> Board.fromEntityWithLike(p, likeCountMap.getOrDefault(p.getId(), 0L)))
                            .collect(toList()))
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Board {
        @Schema(description = "글 ID", example = "1")
        private Long postId;
        @Schema(description = "제목", example = "제목")
        private String title;
        @Schema(description = "닉네임", example = "그린티")
        private String nickname;
        @Schema(description = "카테고리", example = "결혼")
        private String category;
        @Schema(description = "내용", example = "내용")
        private String content;
        @Schema(description = "좋아요", example = "1")
        private Integer likeCount;
        @Schema(description = "작성 날짜", example = "2025.09.09")
        private String createdAt;

        private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        public static Board fromEntity(Community post) {
            return Board.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .nickname(resolveNickname(post))
                    .category(toKoreanCategory(post.getCategory()))
                    .content(post.getContent())
                    .likeCount(Objects.requireNonNullElse(post.getLikeCount(), 0))
                    .createdAt(post.getCreatedAt() != null ? post.getCreatedAt().format(DATE_FMT) : null)
                    .build();
        }

        public static Board fromEntityWithLike(Community post, long likeCount) {
            return Board.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .nickname(resolveNickname(post))
                    .category(toKoreanCategory(post.getCategory()))
                    .content(post.getContent())
                    .likeCount((int) likeCount)
                    .createdAt(post.getCreatedAt() != null ? post.getCreatedAt().format(DATE_FMT) : null)
                    .build();
        }

        private static String resolveNickname(Community post) {
            if (post.getUser() == null) return null;
            if (post.getUser().getNickname() != null) return post.getUser().getNickname();
            if (post.getUser().getName() != null) return post.getUser().getName();
            return null;
        }

        private static String toKoreanCategory(Community.Category cat) {
            if (cat == null) return null;
            switch (cat) {
                case WEDDING: return "결혼";
                case SAVING:  return "저축";
                case HOUSING: return "집 마련";
                case TAX:     return "세금";
                case USED:    return "지출";
                case TIPS:    return "조언";
                case ETC:     return "기타";
                default: return null;
            }
        }
    }
}
