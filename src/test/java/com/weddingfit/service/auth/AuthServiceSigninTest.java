package com.weddingfit.service.auth;

import com.weddingfit.dto.request.auth.SigninRequest;
import com.weddingfit.dto.response.auth.SigninResponse;
import com.weddingfit.entity.user.User;
import com.weddingfit.global.exception.CustomException;
import com.weddingfit.global.exception.GlobalErrorCode;
import com.weddingfit.global.security.JwtProvider;
import com.weddingfit.repository.couple.CoupleRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceSigninTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private CoupleRepository coupleRepository;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    @DisplayName("로그인 성공")
    void signin_success() {
        // Given
        SigninRequest request = new SigninRequest("test1234", "1234");
        
        User user = User.builder()
                .id(1L)
                .loginId("test1234")
                .password("encodedPassword")
                .nickname("그린티")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        given(userRepository.findByLoginIdAndIsActiveTrue("test1234"))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches("1234", "encodedPassword"))
                .willReturn(true);
        org.mockito.Mockito.doNothing()
                .when(refreshTokenRepository).deleteByUserId(1L);
        given(jwtProvider.createAccessToken(1L, "그린티", null))
                .willReturn("jwt-access-token");
        given(coupleRepository.findByUserId(1L))
                .willReturn(Optional.empty());
        
        // When
        SigninResponse response = authService.signin(request);
        
        // Then
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getNickname()).isEqualTo("그린티");
        assertThat(response.getAccessToken()).isEqualTo("jwt-access-token");
        assertThat(response.getCoupleId()).isNull();
    }
    
    @Test
    @DisplayName("존재하지 않는 아이디로 로그인 시도 시 실패")
    void signin_fail_user_not_found() {
        // Given
        SigninRequest request = new SigninRequest("notexist", "1234");
        
        given(userRepository.findByLoginIdAndIsActiveTrue("notexist"))
                .willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> authService.signin(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.INVALID_CREDENTIALS);
    }
    
    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도 시 실패")
    void signin_fail_wrong_password() {
        // Given
        SigninRequest request = new SigninRequest("test1234", "wrongpassword");
        
        User user = User.builder()
                .id(1L)
                .loginId("test1234")
                .password("encodedPassword")
                .nickname("그린티")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        given(userRepository.findByLoginIdAndIsActiveTrue("test1234"))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongpassword", "encodedPassword"))
                .willReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> authService.signin(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.INVALID_CREDENTIALS);
    }
    
    @Test
    @DisplayName("비활성화된 사용자 로그인 시도 시 실패")
    void signin_fail_inactive_user() {
        // Given
        SigninRequest request = new SigninRequest("test1234", "1234");
        
        given(userRepository.findByLoginIdAndIsActiveTrue("test1234"))
                .willReturn(Optional.empty()); // isActive가 false인 유저는 조회되지 않음
        
        // When & Then
        assertThatThrownBy(() -> authService.signin(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.INVALID_CREDENTIALS);
    }
}