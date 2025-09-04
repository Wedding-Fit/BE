package com.weddingfit.service.auth;

import com.weddingfit.dto.request.auth.SignupRequest;
import com.weddingfit.dto.response.auth.SignupResponse;
import com.weddingfit.entity.user.User;
import com.weddingfit.global.exception.CustomException;
import com.weddingfit.global.exception.GlobalErrorCode;
import com.weddingfit.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
