package com.weddingfit.repository.user;

import com.weddingfit.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId); // 로그인시 User 조회용
    boolean existsByLoginId(String loginId); // 회원가입시 중복 검사용
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByNickname(String nickname);
    Optional<User> findByLoginIdAndIsActiveTrue(String loginId); // 활성화된 유저만 조회
}
