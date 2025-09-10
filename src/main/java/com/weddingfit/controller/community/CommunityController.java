package com.weddingfit.controller.community;

import com.weddingfit.dto.request.community.PostlistRequest;
import com.weddingfit.dto.request.community.PostwriteRequest;
import com.weddingfit.dto.response.community.PostdetailResponse;
import com.weddingfit.dto.response.community.PostlistResponse;
import com.weddingfit.dto.response.community.PostwriteResponse;
import com.weddingfit.entity.community.Community;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.global.security.JwtProvider;
import com.weddingfit.service.community.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/community", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Community", description = "커뮤니티 게시판 API")
public class CommunityController {

    private final CommunityService communityService;
    private final JwtProvider jwtProvider;

    @PostMapping(value = "/posts", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "게시글 작성",
            description = "게시글을 등록합니다. (로그인 필수) - Body의 userId는 무시되고 JWT 기준으로 처리됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = PostwriteRequest.class))
            )
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BaseResponse<PostwriteResponse>> createPost(
            @RequestBody PostwriteRequest requestBody,
            HttpServletRequest httpRequest
    ) {
        try {
            Long userId = extractUserId(httpRequest);
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(BaseResponse.error("실패했습니다", "POST_CREATE_UNAUTHORIZED"));
            }

            Community.Category category = mapKoreanCategory(requestBody.getCategory());
            if (category == null) {
                return ResponseEntity.status(400)
                        .body(BaseResponse.error("유효하지 않은 카테고리입니다", "INVALID_CATEGORY"));
            }

            PostwriteResponse response = communityService.createPost(requestBody, userId, category);
            return ResponseEntity.ok(BaseResponse.success(response, "성공했습니다"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(BaseResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("서버 오류가 발생했습니다", "SERVER_ERROR"));
        }
    }

    @GetMapping("/posts")
    @Operation(
            summary = "게시글 목록 조회",
            description = "카테고리별 게시글 목록을 조회합니다. 카테고리는 WEDDING, SAVING, HOUSING, TAX, USED, TIPS, ETC 중 하나입니다.",
            parameters = {
                    @Parameter(
                            name = "category",
                            description = "게시글 카테고리",
                            required = true,
                            in = ParameterIn.QUERY,
                            schema = @Schema(allowableValues = {
                                    "WEDDING","SAVING","HOUSING","TAX","USED","TIPS","ETC"
                            }, type = "string")
                    )
            }
    )
    public ResponseEntity<BaseResponse<PostlistResponse.Data>> getPosts(
            @RequestParam("category") String categoryParam
    ) {
        try {
            PostlistRequest.Category category = PostlistRequest.Category.from(categoryParam);
            if (category == null) {
                return ResponseEntity.status(400)
                        .body(BaseResponse.error("유효하지 않은 카테고리입니다", "INVALID_CATEGORY"));
            }

            PostlistResponse.Data data = communityService.getPostListDataByCategory(category);

            return ResponseEntity.ok(BaseResponse.success(data, "성공했습니다"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("서버 오류가 발생했습니다", "SERVER_ERROR"));
        }
    }

    @GetMapping("/posts/{postId}")
    @Operation(
            summary = "게시글 상세 조회",
            description = "단일 게시글 상세 정보를 조회합니다.",
            parameters = {
                    @Parameter(
                            name = "postId",
                            description = "게시글 ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "number", example = "1")
                    )
            }
    )
    public ResponseEntity<BaseResponse<PostdetailResponse>> getPostDetail(
            @PathVariable("postId") Long postId,
            HttpServletRequest request
    ) {
        try {
            Long currentUserId = extractUserId(request);
            PostdetailResponse detail = communityService.getPostDetail(postId, currentUserId);
            return ResponseEntity.ok(BaseResponse.success(detail, "성공했습니다"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(BaseResponse.error("게시글을 찾을 수 없습니다", "POST_NOT_FOUND"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("서버 오류가 발생했습니다", "SERVER_ERROR"));
        }
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(
            summary = "게시글 삭제",
            description = "게시글을 삭제합니다. (로그인 필수, 본인 글만 삭제 가능)",
            parameters = {
                    @Parameter(name = "postId", required = true, in = ParameterIn.PATH,
                            schema = @Schema(type = "number", example = "1"))
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BaseResponse<Object>> deletePost(
            @PathVariable("postId") Long postId,
            HttpServletRequest request
    ) {
        try {
            Long userId = extractUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(BaseResponse.error("실패했습니다", "POST_DELETE_UNAUTHORIZED"));
            }

            communityService.deletePost(postId, userId);
            return ResponseEntity.ok(BaseResponse.success(null, "성공했습니다"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403)
                    .body(BaseResponse.error(e.getMessage(), "POST_DELETE_FAILED"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("서버 오류가 발생했습니다", "SERVER_ERROR"));
        }
    }


    private Long extractUserId(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (!StringUtils.hasText(token)) return null;
        if (!jwtProvider.isTokenValid(token) || jwtProvider.isRefreshToken(token)) return null;
        try {
            return jwtProvider.getUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth)) {
            auth = auth.trim();
            if (auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return auth.substring(7).trim();
            }
            if (auth.length() > 20) return auth;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c == null) continue;
                if ("accessToken".equalsIgnoreCase(c.getName())
                        || "Authorization".equalsIgnoreCase(c.getName())) {
                    String v = c.getValue();
                    if (!StringUtils.hasText(v)) continue;
                    v = v.trim();
                    if (v.regionMatches(true, 0, "Bearer ", 0, 7)) return v.substring(7).trim();
                    return v;
                }
            }
        }
        return null;
    }

    private Community.Category mapKoreanCategory(String korean) {
        if (korean == null) return null;
        return switch (korean.trim()) {
            case "결혼" -> Community.Category.WEDDING;
            case "저축" -> Community.Category.SAVING;
            case "주거"-> Community.Category.HOUSING;
            case "세금" -> Community.Category.TAX;
            case "지출" -> Community.Category.USED;
            case "조언"   -> Community.Category.TIPS;
            case "기타" -> Community.Category.ETC;
            default -> null;
        };
    }
}
