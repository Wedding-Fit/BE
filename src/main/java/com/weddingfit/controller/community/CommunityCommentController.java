package com.weddingfit.controller.community;

import com.weddingfit.dto.request.community.CommentwriteRequest;
import com.weddingfit.dto.response.community.CommentResponse;
import com.weddingfit.dto.response.community.CommentwriteResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.global.security.JwtProvider;
import com.weddingfit.service.community.CommunityCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/community", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Community Comments", description = "커뮤니티 댓글 기능 API")
public class CommunityCommentController {

    private final CommunityCommentService communityCommentService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "게시글 댓글 조회", description = "특정 게시글의 댓글 목록과 댓글 개수를 조회합니다.")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<BaseResponse<CommentResponse>> getComments(
            @Parameter(description = "게시글 ID") @PathVariable Long postId
    ) {
        try {
            CommentResponse data = communityCommentService.getPostComments(postId);
            return ResponseEntity.ok(BaseResponse.success(data, "성공했습니다"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("요청이 올바르지 않습니다", "COMMENT_LIST_FAILED"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("서버 오류가 발생했습니다", "SERVER_ERROR"));
        }
    }

    @Operation(
            summary = "댓글 작성",
            description = "특정 게시글에 댓글을 작성합니다. (로그인 필수)"
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse<CommentwriteResponse>> createComment(
            @RequestParam Long postId,
            @Valid @RequestBody CommentwriteRequest body,
            HttpServletRequest request,
            @AuthenticationPrincipal Object principalFromAnnotation
    ) {
        try {
            Long userId = extractUserIdFromSecurityContext(principalFromAnnotation);
            if (userId == null) {
                String authorization = request.getHeader("Authorization");
                if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
                    return ResponseEntity.status(401)
                            .body(BaseResponse.error("로그인이 필요합니다", "UNAUTHORIZED"));
                }
                String token = authorization.substring(7);
                if (!jwtProvider.isTokenValid(token)) {
                    return ResponseEntity.status(401)
                            .body(BaseResponse.error("유효하지 않은 토큰입니다", "INVALID_TOKEN"));
                }
                userId = jwtProvider.getUserId(token);
            }

            Long commentId = communityCommentService.createComment(postId, userId, body.getContent());
            CommentwriteResponse data = CommentwriteResponse.builder()
                    .commentId(commentId)
                    .build();

            return ResponseEntity.ok(BaseResponse.success(data, "성공했습니다"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("요청이 올바르지 않습니다", "COMMENT_CREATE_FAILED"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("서버 오류가 발생했습니다", "SERVER_ERROR"));
        }
    }

    private Long extractUserIdFromSecurityContext(Object principalFromAnnotation) {
        Long id = coerceToLongOrNull(principalFromAnnotation);
        if (id != null) return id;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;

        Object principal = authentication.getPrincipal();
        id = coerceToLongOrNull(principal);
        if (id != null) return id;

        try {
            var m = principal.getClass().getMethod("getId");
            Object val = m.invoke(principal);
            return coerceToLongOrNull(val);
        } catch (Exception ignored) { /* no-op */ }

        try {
            var m = principal.getClass().getMethod("getUserId");
            Object val = m.invoke(principal);
            return coerceToLongOrNull(val);
        } catch (Exception ignored) { /* no-op */ }

        try {
            var m = principal.getClass().getMethod("getUsername");
            Object val = m.invoke(principal);
            return coerceToLongOrNull(val);
        } catch (Exception ignored) { /* no-op */ }

        try {
            var m = principal.getClass().getMethod("getName");
            Object val = m.invoke(principal);
            return coerceToLongOrNull(val);
        } catch (Exception ignored) { /* no-op */ }

        return null;
    }

    private Long coerceToLongOrNull(Object o) {
        if (o == null) return null;
        if (o instanceof Long l) return l;
        if (o instanceof Integer i) return i.longValue();
        if (o instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) { /* not numeric */ }
        }
        return null;
    }
}
