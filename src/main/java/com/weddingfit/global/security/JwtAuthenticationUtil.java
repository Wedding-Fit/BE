package com.weddingfit.global.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationUtil {
    
    private final JwtProvider jwtProvider;
    
    public Long getCurrentUserId(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtProvider.isTokenValid(token)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }
        return jwtProvider.getUserId(token);
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}