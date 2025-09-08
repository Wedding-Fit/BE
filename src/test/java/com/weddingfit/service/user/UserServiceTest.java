package com.weddingfit.service.user;

import com.weddingfit.entity.user.User;
import com.weddingfit.global.exception.CustomException;
import com.weddingfit.global.exception.GlobalErrorCode;
import com.weddingfit.repository.user.RefreshTokenRepository;
import com.weddingfit.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteUser_success() {
        // Given
        Long userId = 1L;
        User activeUser = User.builder()
                .id(userId)
                .loginId("test1234")
                .password("encodedPassword")
                .name("테스트")
                .nickname("테츠")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        given(userRepository.findById(userId)).willReturn(Optional.of(activeUser));
        
        // When
        userService.deleteUser(userId);
        
        // Then
        verify(refreshTokenRepository).deleteByUserId(userId);
        verify(userRepository).delete(activeUser);
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 탈퇴 시도 시 실패")
    void deleteUser_fail_user_not_found() {
        // Given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.NOT_FOUND);
    }
    
    @Test
    @DisplayName("이미 탈퇴한 사용자 탈퇴 시도 시 실패")
    void deleteUser_fail_already_deleted() {
        // Given
        Long userId = 1L;
        User inactiveUser = User.builder()
                .id(userId)
                .loginId("test1234")
                .password("encodedPassword")
                .name("테스트")
                .nickname("테츠")
                .isActive(false) // 이미 비활성화됨
                .createdAt(LocalDateTime.now())
                .build();
        
        given(userRepository.findById(userId)).willReturn(Optional.of(inactiveUser));
        
        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.NOT_FOUND);
    }
    
    @Test
    @DisplayName("사용자 조회 성공")
    void getUserById_success() {
        // Given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .loginId("test1234")
                .name("테스트")
                .nickname("테츠")
                .isActive(true)
                .build();
        
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        
        // When
        User result = userService.getUserById(userId);
        
        // Then
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getLoginId()).isEqualTo("test1234");
        assertThat(result.getName()).isEqualTo("테스트");
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 실패")
    void getUserById_fail_user_not_found() {
        // Given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.NOT_FOUND);
    }
}