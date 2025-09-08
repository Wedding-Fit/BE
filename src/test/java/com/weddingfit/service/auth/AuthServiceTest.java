package com.weddingfit.service.auth;

import com.weddingfit.dto.request.auth.SignupRequest;
import com.weddingfit.dto.response.auth.SignupResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

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
    @DisplayName("회원가입 성공")
    void 회원가입_성공() {
        // Given
        SignupRequest request = new SignupRequest(
                "greentea123",
                "password123!",
                "김보성",
                "그린티",
                "1999-08-30",
                "MALE",
                "010-1234-5678"
        );

        User savedUser = User.builder()
                .id(1L)
                .loginId("greentea123")
                .password("encodedPassword")
                .name("김보성")
                .nickname("그린티")
                .birth(LocalDate.of(1999, 8, 30))
                .gender(User.Gender.MALE)
                .phoneNumber("010-1234-5678")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.existsByLoginId("greentea123")).willReturn(false);
        given(userRepository.existsByPhoneNumber("010-1234-5678")).willReturn(false);
        given(userRepository.existsByNickname("그린티")).willReturn(false);
        given(passwordEncoder.encode("password123!")).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // When
        SignupResponse response = authService.signup(request);

        // Then
        assertThat(response.getLoginId()).isEqualTo("greentea123");
        assertThat(response.getName()).isEqualTo("김보성");
        assertThat(response.getNickname()).isEqualTo("그린티");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("아이디 중복으로 회원가입 실패")
    void 아이디중복_예외발생() {
        // Given
        SignupRequest request = new SignupRequest(
                "dupUser123",
                "password123!",
                "테츠",
                "테츠츠",
                "2000-01-01",
                "MALE",
                "010-1234-5678"
        );

        given(userRepository.existsByLoginId("dupUser123")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.DUPLICATE_LOGIN_ID);
    }

    @Test
    @DisplayName("닉네임 중복으로 회원가입 실패")
    void 닉네임중복_예외발생() {
        // Given
        SignupRequest request = new SignupRequest(
                "user2",
                "password123!",
                "이영희",
                "중복닉네임",
                "2000-01-01",
                "FEMALE",
                "010-3333-4444"
        );

        given(userRepository.existsByLoginId("user2")).willReturn(false);
        given(userRepository.existsByPhoneNumber("010-3333-4444")).willReturn(false);
        given(userRepository.existsByNickname("중복닉네임")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    @DisplayName("전화번호 중복으로 회원가입 실패")
    void 전화번호중복_예외발생() {
        // Given
        SignupRequest request = new SignupRequest(
                "user2",
                "password123!",
                "이영희",
                "영희",
                "2000-01-01",
                "FEMALE",
                "010-5555-6666"
        );

        given(userRepository.existsByLoginId("user2")).willReturn(false);
        given(userRepository.existsByPhoneNumber("010-5555-6666")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.DUPLICATE_PHONE_NUMBER);
    }
}