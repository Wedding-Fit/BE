package com.weddingfit.service.user;

import com.weddingfit.entity.user.User;
import com.weddingfit.global.exception.CustomException;
import com.weddingfit.global.exception.GlobalErrorCode;
import com.weddingfit.repository.user.RefreshTokenRepository;
import com.weddingfit.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND));
    }
    
    @Transactional(readOnly = true)
    public User findByLoginId(String loginId) {
        return userRepository.findByLoginIdAndIsActiveTrue(loginId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND));
    }
    
    @Transactional
    public void deleteUser(Long userId) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND));
        
        // 이미 탈퇴한 사용자인지 확인
        if (!user.getIsActive()) {
            throw new CustomException(GlobalErrorCode.NOT_FOUND);
        }
        refreshTokenRepository.deleteByUserId(userId); // 연관된 refresh token 먼저 삭제
        userRepository.delete(user); // 사용자 완전 삭제
        
        log.info("User {} has been permanently deleted", userId);
    }
}