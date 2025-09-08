package com.weddingfit.service.auth;

import com.weddingfit.dto.request.auth.RefreshTokenRequest;
import com.weddingfit.dto.request.auth.SigninRequest;
import com.weddingfit.dto.request.auth.SignoutRequest;
import com.weddingfit.dto.request.auth.SignupRequest;
import com.weddingfit.dto.response.auth.RefreshTokenResponse;
import com.weddingfit.dto.response.auth.SigninResponse;
import com.weddingfit.dto.response.auth.SignupResponse;
import com.weddingfit.entity.user.RefreshToken;
import com.weddingfit.entity.user.User;
import com.weddingfit.global.exception.CustomException;
import com.weddingfit.global.exception.GlobalErrorCode;
import com.weddingfit.global.security.JwtProvider;
import com.weddingfit.repository.user.RefreshTokenRepository;
import com.weddingfit.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public SignupResponse signup(SignupRequest request){
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new CustomException(GlobalErrorCode.DUPLICATE_LOGIN_ID);
        }
        if(userRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new CustomException(GlobalErrorCode.DUPLICATE_PHONE_NUMBER);
        }
        if(request.getNickname() != null && userRepository.existsByNickname(request.getNickname())){
            throw new CustomException(GlobalErrorCode.DUPLICATE_NICKNAME);
        }
        
        // String을 Entity 타입으로 변환
        LocalDate birth = request.getBirth() != null ? 
            LocalDate.parse(request.getBirth(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
        User.Gender gender = request.getGender() != null ? 
            User.Gender.valueOf(request.getGender()) : null;
        
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        User user = User.builder()
                .loginId(request.getLoginId())
                .password(encodedPassword)
                .name(request.getName())
                .nickname(request.getNickname())
                .birth(birth)
                .gender(gender)
                .phoneNumber(request.getPhoneNumber())
                .build();
        
        User savedUser = userRepository.save(user);
        
        return SignupResponse.builder()
                .userId(savedUser.getId())
                .loginId(savedUser.getLoginId())
                .name(savedUser.getName())
                .nickname(savedUser.getNickname())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }
    @Transactional
    public SigninResponse signin(SigninRequest request){
        User user = userRepository.findByLoginIdAndIsActiveTrue(request.getLoginId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(GlobalErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getNickname(),
                null
        );
        
        // 기존 리프레시 토큰 삭제 후 새로 생성
        refreshTokenRepository.deleteByUserId(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plus(7, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);
        
        return SigninResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .coupleId(null)
                .build();
    }
    
    @Transactional
    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // 토큰 유효성 검증
        if (!jwtProvider.isTokenValid(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new CustomException(GlobalErrorCode.LOGIN_REQUIRED);
        }
        
        // DB에서 리프레시 토큰 조회
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.LOGIN_REQUIRED));
        
        // 토큰 만료 확인
        if (tokenEntity.isExpired()) {
            refreshTokenRepository.delete(tokenEntity);
            throw new CustomException(GlobalErrorCode.LOGIN_REQUIRED);
        }
        
        // 사용자 정보 조회
        Long userId = jwtProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.LOGIN_REQUIRED));
        
        // 새로운 액세스 토큰 생성
        String newAccessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getNickname(),
                null
        );
        
        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }
    
    @Transactional
    public void signout(SignoutRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // 토큰 유효성 검증
        if (!jwtProvider.isTokenValid(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new CustomException(GlobalErrorCode.LOGIN_REQUIRED);
        }
        
        // DB에서 리프레시 토큰 삭제
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.LOGIN_REQUIRED));
        
        refreshTokenRepository.delete(tokenEntity);
        log.info("User {} signed out successfully", jwtProvider.getUserId(refreshToken));
    }
}
