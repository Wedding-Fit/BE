package com.weddingfit.service.auth;

import com.weddingfit.dto.request.auth.RefreshTokenRequest;
import com.weddingfit.dto.response.auth.RefreshTokenResponse;
import com.weddingfit.entity.user.RefreshToken;
import com.weddingfit.entity.user.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceRefreshTokenTest {

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
    @DisplayName("토큰 재발급 성공")
    void refreshAccessToken_success() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        
        User user = User.builder()
                .id(1L)
                .loginId("test1234")
                .nickname("그린티")
                .isActive(true)
                .build();
        
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
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(jwtProvider.createAccessToken(1L, "그린티", null))
                .willReturn("new-access-token");
        
        // When
        RefreshTokenResponse response = authService.refreshAccessToken(request);
        
        // Then
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    }
    
    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 재발급 시도 시 실패")
    void refreshAccessToken_fail_invalid_token() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");
        
        given(jwtProvider.isTokenValid("invalid-refresh-token")).willReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> authService.refreshAccessToken(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.LOGIN_REQUIRED);
    }
    
    @Test
    @DisplayName("리프레시 토큰이 아닌 토큰으로 재발급 시도 시 실패")
    void refreshAccessToken_fail_not_refresh_token() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("access-token");
        
        given(jwtProvider.isTokenValid("access-token")).willReturn(true);
        given(jwtProvider.isRefreshToken("access-token")).willReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> authService.refreshAccessToken(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.LOGIN_REQUIRED);
    }
    
    @Test
    @DisplayName("DB에 존재하지 않는 리프레시 토큰으로 재발급 시도 시 실패")
    void refreshAccessToken_fail_token_not_found() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("nonexistent-refresh-token");
        
        given(jwtProvider.isTokenValid("nonexistent-refresh-token")).willReturn(true);
        given(jwtProvider.isRefreshToken("nonexistent-refresh-token")).willReturn(true);
        given(refreshTokenRepository.findByToken("nonexistent-refresh-token"))
                .willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> authService.refreshAccessToken(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.LOGIN_REQUIRED);
    }
    
    @Test
    @DisplayName("만료된 리프레시 토큰으로 재발급 시도 시 실패")
    void refreshAccessToken_fail_expired_token() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("expired-refresh-token");
        
        RefreshToken expiredToken = RefreshToken.builder()
                .id(1L)
                .token("expired-refresh-token")
                .userId(1L)
                .expiresAt(LocalDateTime.now().minusDays(1)) // 만료된 토큰
                .build();
        
        given(jwtProvider.isTokenValid("expired-refresh-token")).willReturn(true);
        given(jwtProvider.isRefreshToken("expired-refresh-token")).willReturn(true);
        given(refreshTokenRepository.findByToken("expired-refresh-token"))
                .willReturn(Optional.of(expiredToken));
        
        // When & Then
        assertThatThrownBy(() -> authService.refreshAccessToken(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.LOGIN_REQUIRED);
        
        // 만료된 토큰은 DB에서 삭제되어야 함
        verify(refreshTokenRepository).delete(expiredToken);
    }
}