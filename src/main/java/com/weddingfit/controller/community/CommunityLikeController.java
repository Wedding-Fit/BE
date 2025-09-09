package com.weddingfit.controller.community;

import com.weddingfit.dto.response.community.LikeDeleteResponse;
import com.weddingfit.dto.response.community.LikeResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.global.security.JwtProvider;
import com.weddingfit.service.community.CommunityLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/community", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Community Likes", description = "커뮤니티 좋아요 기능 API")
public class CommunityLikeController {

    private final CommunityLikeService communityLikeService;
    private final JwtProvider jwtProvider;

    static class BadRequestException extends RuntimeException {
        BadRequestException(String msg) { super(msg); }
    }

    private Long resolveCurrentUserId(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Long id = coerceToLongOrNull(auth.getPrincipal());
            if (id != null) return id;
            id = coerceToLongOrNull(auth.getName());
            if (id != null) return id;
        }

        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization)) {
            // 3) 쿠키 accessToken / Authorization
            String cookieToken = extractTokenFromCookies(request.getCookies());
            if (StringUtils.hasText(cookieToken)) {
                authorization = cookieToken.startsWith("Bearer ")
                        ? cookieToken
                        : "Bearer " + cookieToken;
            }
        }

        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return null;
        }

        String token = authorization.substring(7).trim();
        if (!jwtProvider.isTokenValid(token)) {
            log.debug("JWT invalid: {}", token);
            return null;
        }

        try {
            return jwtProvider.getUserId(token);
        } catch (Exception e) {
            log.debug("토큰에서 userId 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    private String extractTokenFromCookies(Cookie[] cookies) {
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (c == null) continue;
            if ("accessToken".equalsIgnoreCase(c.getName())
                    || "Authorization".equalsIgnoreCase(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    private Long coerceToLongOrNull(Object o) {
        if (o == null) return null;
        if (o instanceof Long l) return l;
        if (o instanceof Integer i) return i.longValue();
        if (o instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    @PostMapping(value = "/posts/{postId}/likes", consumes = MediaType.ALL_VALUE)
    @Operation(
            summary = "게시글 좋아요",
            description = "로그인된 사용자가 게시글에 좋아요를 추가합니다.",
            parameters = { @Parameter(name = "postId", required = true, in = ParameterIn.PATH) }
    )
    public ResponseEntity<BaseResponse<LikeResponse>> likePost(
            @PathVariable("postId") Long postId,
            HttpServletRequest request
    ) {
        log.info("[Like] POST /posts/{}/likes", postId);
        try {
            if (postId == null || postId <= 0) throw new BadRequestException("postId가 올바르지 않습니다");

            Long userId = resolveCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.ok(BaseResponse.error("로그인이 필요합니다", "UNAUTHORIZED"));
            }

            communityLikeService.like(postId, userId);

            LikeResponse data = LikeResponse.builder().placeholder(null).build();
            return ResponseEntity.ok(BaseResponse.success(data, "좋아요 성공했습니다"));

        } catch (BadRequestException | IllegalArgumentException e) {
            return ResponseEntity.ok(BaseResponse.error(e.getMessage(), "LIKE_FAILED")); // 400 대신 200
        } catch (Exception e) {
            log.error("좋아요 처리 중 서버 오류", e);
            return ResponseEntity.ok(BaseResponse.error("서버 오류가 발생했습니다", "SERVER_ERROR")); // 500 대신 200
        }
    }

    @DeleteMapping(value = "/posts/{postId}/likes", consumes = MediaType.ALL_VALUE)
    @Operation(
            summary = "게시글 좋아요 취소",
            description = "로그인된 사용자가 게시글 좋아요를 취소합니다.",
            parameters = { @Parameter(name = "postId", required = true, in = ParameterIn.PATH) }
    )
    public ResponseEntity<BaseResponse<LikeDeleteResponse>> unlikePost(
            @PathVariable("postId") Long postId,
            HttpServletRequest request
    ) {
        log.info("[Like] DELETE /posts/{}/likes", postId);
        try {
            if (postId == null || postId <= 0) throw new BadRequestException("postId가 올바르지 않습니다");

            Long userId = resolveCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.ok(BaseResponse.error("로그인이 필요합니다", "UNAUTHORIZED"));
            }

            communityLikeService.unlike(postId, userId);

            LikeDeleteResponse data = LikeDeleteResponse.builder().placeholder(null).build();
            return ResponseEntity.ok(BaseResponse.success(data, "좋아요 취소했습니다"));

        } catch (BadRequestException | IllegalArgumentException e) {
            return ResponseEntity.ok(BaseResponse.error(e.getMessage(), "LIKE_CANCEL_FAILED")); // 400 대신 200
        } catch (Exception e) {
            log.error("좋아요 취소 처리 중 서버 오류", e);
            return ResponseEntity.ok(BaseResponse.error("서버 오류가 발생했습니다", "SERVER_ERROR")); // 500 대신 200
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Object>> handleTypeMismatch() {
        return ResponseEntity.ok(BaseResponse.error("요청이 올바르지 않습니다", "BAD_REQUEST"));
    }
}
