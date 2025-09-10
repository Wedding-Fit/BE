package com.weddingfit.service.couple;

import com.weddingfit.dto.request.couple.CoupleRegisterRequest;
import com.weddingfit.dto.request.couple.CoupleUpdateRequest;
import com.weddingfit.dto.response.couple.CoupleInfoResponse;
import com.weddingfit.dto.response.couple.CoupleRegisterResponse;
import com.weddingfit.dto.response.user.CoupleNamesResponse;
import com.weddingfit.entity.couple.Couple;
import com.weddingfit.entity.user.User;
import com.weddingfit.global.exception.CustomException;
import com.weddingfit.global.exception.GlobalErrorCode;
import com.weddingfit.repository.couple.CoupleRepository;
import com.weddingfit.repository.user.UserRepository;
import com.weddingfit.repository.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoupleService {
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    
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

    public CoupleNamesResponse getCoupleNames(Long coupleId, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));

        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.COUPLE_NOT_FOUND));

        if (!couple.getUser1().getId().equals(currentUserId) && 
            !couple.getUser2().getId().equals(currentUserId)) {
            throw new CustomException(GlobalErrorCode.FORBIDDEN);
        }

        User user1 = couple.getUser1();
        User user2 = couple.getUser2();

        String femaleName = null;
        String maleName = null;

        if (user1.getGender() == User.Gender.FEMALE) {
            femaleName = user1.getName();
            maleName = user2.getName();
        } else {
            femaleName = user2.getName();
            maleName = user1.getName();
        }

        return CoupleNamesResponse.builder()
                .femaleName(femaleName)
                .maleName(maleName)
                .weddingDate(couple.getWeddingDate())
                .build();
    }
    
    /**
     * 커플의 총 자산을 계산하고 업데이트
     * @param coupleId 커플 ID
     */
    @Transactional
    public void updateCoupleTotalAmount(Long coupleId) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.COUPLE_NOT_FOUND));
        
        // 두 사용자의 모든 계좌 잔액 합계 계산
        BigDecimal user1TotalBalance = accountRepository.findByUser(couple.getUser1())
                .stream()
                .map(account -> account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal user2TotalBalance = accountRepository.findByUser(couple.getUser2())
                .stream()
                .map(account -> account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalAmount = user1TotalBalance.add(user2TotalBalance);
        
        // 커플 총 자산 업데이트
        couple.updateTotalAmount(totalAmount);
        coupleRepository.save(couple);
    }
    
    /**
     * 모든 커플의 총 자산을 일괄 업데이트
     */
    @Transactional
    public void updateAllCouplesTotalAmount() {
        coupleRepository.findAll().forEach(couple -> {
            try {
                updateCoupleTotalAmount(couple.getId());
            } catch (Exception e) {
                // 개별 커플 업데이트 실패 시 로그만 남기고 계속 진행
                System.err.println("커플 총 자산 업데이트 실패: coupleId=" + couple.getId() + ", error=" + e.getMessage());
            }
        });
    }
}