package com.weddingfit.service.auth;

import com.weddingfit.dto.request.auth.SignupRequest;
import com.weddingfit.dto.response.auth.SignupResponse;
import com.weddingfit.entity.user.User;
import com.weddingfit.global.exception.CustomException;
import com.weddingfit.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void 회원가입_성공() {
        SignupRequest request = new SignupRequest(
                "greentea123",
                "password123!",
                "김보성",
                "그린티",
                "1999-08-30",
                "MALE",
                "010-1234-5678"
        );

        SignupResponse response = authService.signup(request);

        assertThat(response.getLoginId()).isEqualTo("greentea123");
        assertThat(userRepository.findByLoginId("greentea123")).isPresent();
    }

    @Test
    void 아이디중복_예외발생() {
        // given
        User existingUser = User.builder()
                .loginId("dupUser123")
                .password("encodedPassword")
                .name("김보성")
                .phoneNumber("010-9999-8888")
                .isActive(true)
                .build();
        userRepository.saveAndFlush(existingUser);

        // when & then
        SignupRequest request = new SignupRequest(
                "dupUser123",   // 동일한 아이디로 요청
                "password123!",
                "테츠",
                "테츠츠",
                "2000-01-01",
                "MALE",
                "010-1234-5678"
        );

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 사용 중인 아이디입니다");
    }

    @Test
    void 닉네임중복_예외발생() {
        // given
        User existingUser = User.builder()
                .loginId("user1")
                .password("encodedPassword")
                .name("김철수")
                .nickname("중복닉네임")
                .phoneNumber("010-1111-2222")
                .isActive(true)
                .build();
        userRepository.save(existingUser);

        // when & then
        SignupRequest request = new SignupRequest(
                "user2",
                "password123!",
                "이영희",
                "중복닉네임",
                "2000-01-01",
                "FEMALE",
                "010-3333-4444"
        );

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 사용 중인 닉네임입니다");
    }

    @Test
    void 전화번호중복_예외발생() {
        // given
        User existingUser = User.builder()
                .loginId("user1")
                .password("encodedPassword")
                .name("김철수")
                .phoneNumber("010-5555-6666")
                .isActive(true)
                .build();
        userRepository.save(existingUser);

        // when & then
        SignupRequest request = new SignupRequest(
                "user2",
                "password123!",
                "이영희",
                "영희",
                "2000-01-01",
                "FEMALE",
                "010-5555-6666"
        );

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 사용 중인 전화번호입니다");
    }
}