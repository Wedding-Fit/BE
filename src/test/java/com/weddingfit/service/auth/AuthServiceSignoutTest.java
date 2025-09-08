package com.weddingfit.service.auth;

import com.weddingfit.dto.request.auth.SignoutRequest;
import com.weddingfit.entity.user.RefreshToken;
import com.weddingfit.global.exception.CustomException;
import com.weddingfit.global.exception.GlobalErrorCode;
import com.weddingfit.global.security.JwtProvider;
import com.weddingfit.repository.user.RefreshTokenRepository;
import com.weddingfit.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceSignoutTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtProvider jwtProvider;
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    @DisplayName("로그아웃 성공")
    void signout_success() {
        // Given
        SignoutRequest request = new SignoutRequest("valid-refresh-token");
        
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("valid-refresh-token")
                .userId(1L)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        given(jwtProvider.isTokenValid("valid-refresh-token")).willReturn(true);
        given(jwtProvider.isRefreshToken("valid-refresh-token")).willReturn(true);
        given(refreshTokenRepository.findByToken("valid-refresh-token"))
                .willReturn(Optional.of(refreshToken));
        given(jwtProvider.getUserId("valid-refresh-token")).willReturn(1L);
        
        // When
        authService.signout(request);
        
        // Then
        verify(refreshTokenRepository).delete(refreshToken);
    }
    
    @Test
    @DisplayName("유효하지 않은 토큰으로 로그아웃 시도 시 실패")
    void signout_fail_invalid_token() {
        // Given
        SignoutRequest request = new SignoutRequest("invalid-token");
        
        given(jwtProvider.isTokenValid("invalid-token")).willReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> authService.signout(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.LOGIN_REQUIRED);
    }
    
    @Test
    @DisplayName("리프레시 토큰이 아닌 토큰으로 로그아웃 시도 시 실패")
    void signout_fail_not_refresh_token() {
        // Given
        SignoutRequest request = new SignoutRequest("access-token");
        
        given(jwtProvider.isTokenValid("access-token")).willReturn(true);
        given(jwtProvider.isRefreshToken("access-token")).willReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> authService.signout(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.LOGIN_REQUIRED);
    }
    
    @Test
    @DisplayName("DB에 존재하지 않는 토큰으로 로그아웃 시도 시 실패")
    void signout_fail_token_not_found() {
        // Given
        SignoutRequest request = new SignoutRequest("nonexistent-token");
        
        given(jwtProvider.isTokenValid("nonexistent-token")).willReturn(true);
        given(jwtProvider.isRefreshToken("nonexistent-token")).willReturn(true);
        given(refreshTokenRepository.findByToken("nonexistent-token"))
                .willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> authService.signout(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.LOGIN_REQUIRED);
    }
}