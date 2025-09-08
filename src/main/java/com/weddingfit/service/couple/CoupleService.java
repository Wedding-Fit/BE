package com.weddingfit.service.couple;

import com.weddingfit.dto.request.couple.CoupleRegisterRequest;
import com.weddingfit.dto.response.couple.CoupleRegisterResponse;
import com.weddingfit.entity.couple.Couple;
import com.weddingfit.entity.user.User;
import com.weddingfit.global.exception.CustomException;
import com.weddingfit.global.exception.GlobalErrorCode;
import com.weddingfit.repository.couple.CoupleRepository;
import com.weddingfit.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoupleService {
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public CoupleRegisterResponse registerCouple(CoupleRegisterRequest request, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));
        
        User partner = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));

        if (currentUser.getId().equals(partner.getId())) {
            throw new CustomException(GlobalErrorCode.CANNOT_COUPLE_WITH_SELF);
        }
        if (coupleRepository.existsByMember(currentUser.getId())) {
            throw new CustomException(GlobalErrorCode.COUPLE_ALREADY_EXISTS);
        }
        if (coupleRepository.existsByMember(partner.getId())) {
            throw new CustomException(GlobalErrorCode.COUPLE_ALREADY_EXISTS);
        }

        Couple couple = Couple.builder()
                .user1(currentUser)
                .user2(partner)
                .region(request.getRegion())
                .weddingType(request.getWeddingType())
                .honeymoonBudget(request.getHoneymoonBudget())
                .photoPackage(request.getPhotoPackage())
                .dressMakeup(request.getDressMakeup())
                .weddingDate(request.getWeddingDate())
                .build();
        
        Couple savedCouple = coupleRepository.save(couple);
        
        return CoupleRegisterResponse.builder()
                .coupleId(savedCouple.getId())
                .build();
    }
}