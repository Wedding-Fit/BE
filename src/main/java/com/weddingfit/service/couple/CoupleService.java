package com.weddingfit.service.couple;

import com.weddingfit.dto.request.couple.CoupleRegisterRequest;
import com.weddingfit.dto.request.couple.CoupleUpdateRequest;
import com.weddingfit.dto.response.couple.CoupleInfoResponse;
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

    @Transactional
    public CoupleRegisterResponse updateCouple(CoupleUpdateRequest request, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));

        Couple couple = coupleRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.COUPLE_NOT_FOUND));

        Couple updatedCouple = Couple.builder()
                .id(couple.getId())
                .user1(couple.getUser1())
                .user2(couple.getUser2())
                .totalAmount(couple.getTotalAmount())
                .region(request.getRegion())
                .weddingType(request.getWeddingType())
                .honeymoonBudget(request.getHoneymoonBudget())
                .photoPackage(request.getPhotoPackage())
                .dressMakeup(request.getDressMakeup())
                .weddingDate(request.getWeddingDate())
                .createdAt(couple.getCreatedAt())
                .build();

        Couple savedCouple = coupleRepository.save(updatedCouple);

        return CoupleRegisterResponse.builder()
                .coupleId(savedCouple.getId())
                .build();
    }

    public CoupleInfoResponse getCoupleInfo(Long coupleId, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));

        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.COUPLE_NOT_FOUND));

        if (!couple.getUser1().getId().equals(currentUserId) && 
            !couple.getUser2().getId().equals(currentUserId)) {
            throw new CustomException(GlobalErrorCode.FORBIDDEN);
        }

        User partner = couple.getUser1().getId().equals(currentUserId) ? 
                couple.getUser2() : couple.getUser1();

        return CoupleInfoResponse.builder()
                .loginId(partner.getLoginId())
                .region(couple.getRegion())
                .weddingType(couple.getWeddingType())
                .honeymoonBudget(couple.getHoneymoonBudget())
                .photoPackage(couple.getPhotoPackage())
                .dressMakeup(couple.getDressMakeup())
                .weddingDate(couple.getWeddingDate())
                .build();
    }
}