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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CoupleServiceTest {

    @Mock
    private CoupleRepository coupleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CoupleService coupleService;

    @Test
    @DisplayName("커플 등록 성공")
    void registerCouple_success() {
        // Given
        Long currentUserId = 1L;
        CoupleRegisterRequest request = new CoupleRegisterRequest(
                "partner123",
                "서울특별시",
                "스몰 웨딩",
                true,
                false,
                true,
                LocalDate.of(2025, 8, 30)
        );

        User currentUser = User.builder()
                .id(1L)
                .loginId("user123")
                .nickname("현재사용자")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        User partner = User.builder()
                .id(2L)
                .loginId("partner123")
                .nickname("파트너")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        Couple savedCouple = Couple.builder()
                .id(1L)
                .user1(currentUser)
                .user2(partner)
                .region("서울특별시")
                .weddingType("스몰 웨딩")
                .honeymoonBudget(true)
                .photoPackage(false)
                .dressMakeup(true)
                .weddingDate(LocalDate.of(2025, 8, 30))
                .build();

        given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentUser));
        given(userRepository.findByLoginId("partner123")).willReturn(Optional.of(partner));
        given(coupleRepository.existsByMember(1L)).willReturn(false);
        given(coupleRepository.existsByMember(2L)).willReturn(false);
        given(coupleRepository.save(any(Couple.class))).willReturn(savedCouple);

        // When
        CoupleRegisterResponse response = coupleService.registerCouple(request, currentUserId);

        // Then
        assertThat(response.getCoupleId()).isEqualTo(1L);
        verify(coupleRepository).save(any(Couple.class));
    }

    @Test
    @DisplayName("존재하지 않는 현재 사용자 ID로 커플 등록 실패")
    void registerCouple_fail_current_user_not_found() {
        // Given
        Long currentUserId = 999L;
        CoupleRegisterRequest request = new CoupleRegisterRequest(
                "partner123",
                "서울특별시",
                "스몰 웨딩",
                true,
                false,
                true,
                LocalDate.of(2025, 8, 30)
        );

        given(userRepository.findById(currentUserId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> coupleService.registerCouple(request, currentUserId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 파트너 로그인 ID로 커플 등록 실패")
    void registerCouple_fail_partner_not_found() {
        // Given
        Long currentUserId = 1L;
        CoupleRegisterRequest request = new CoupleRegisterRequest(
                "notexist123",
                "서울특별시",
                "스몰 웨딩",
                true,
                false,
                true,
                LocalDate.of(2025, 8, 30)
        );

        User currentUser = User.builder()
                .id(1L)
                .loginId("user123")
                .nickname("현재사용자")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentUser));
        given(userRepository.findByLoginId("notexist123")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> coupleService.registerCouple(request, currentUserId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("본인과 커플 등록 시도 시 실패")
    void registerCouple_fail_cannot_couple_with_self() {
        // Given
        Long currentUserId = 1L;
        CoupleRegisterRequest request = new CoupleRegisterRequest(
                "user123",
                "서울특별시",
                "스몰 웨딩",
                true,
                false,
                true,
                LocalDate.of(2025, 8, 30)
        );

        User currentUser = User.builder()
                .id(1L)
                .loginId("user123")
                .nickname("현재사용자")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentUser));
        given(userRepository.findByLoginId("user123")).willReturn(Optional.of(currentUser));

        // When & Then
        assertThatThrownBy(() -> coupleService.registerCouple(request, currentUserId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.CANNOT_COUPLE_WITH_SELF);
    }

    @Test
    @DisplayName("현재 사용자가 이미 커플에 소속되어 있을 때 등록 실패")
    void registerCouple_fail_current_user_already_in_couple() {
        // Given
        Long currentUserId = 1L;
        CoupleRegisterRequest request = new CoupleRegisterRequest(
                "partner123",
                "서울특별시",
                "스몰 웨딩",
                true,
                false,
                true,
                LocalDate.of(2025, 8, 30)
        );

        User currentUser = User.builder()
                .id(1L)
                .loginId("user123")
                .nickname("현재사용자")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        User partner = User.builder()
                .id(2L)
                .loginId("partner123")
                .nickname("파트너")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentUser));
        given(userRepository.findByLoginId("partner123")).willReturn(Optional.of(partner));
        given(coupleRepository.existsByMember(1L)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> coupleService.registerCouple(request, currentUserId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.COUPLE_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("파트너가 이미 커플에 소속되어 있을 때 등록 실패")
    void registerCouple_fail_partner_already_in_couple() {
        // Given
        Long currentUserId = 1L;
        CoupleRegisterRequest request = new CoupleRegisterRequest(
                "partner123",
                "서울특별시",
                "스몰 웨딩",
                true,
                false,
                true,
                LocalDate.of(2025, 8, 30)
        );

        User currentUser = User.builder()
                .id(1L)
                .loginId("user123")
                .nickname("현재사용자")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        User partner = User.builder()
                .id(2L)
                .loginId("partner123")
                .nickname("파트너")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentUser));
        given(userRepository.findByLoginId("partner123")).willReturn(Optional.of(partner));
        given(coupleRepository.existsByMember(1L)).willReturn(false);
        given(coupleRepository.existsByMember(2L)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> coupleService.registerCouple(request, currentUserId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.COUPLE_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("커플 정보 수정 성공")
    void updateCouple_success() {
        // Given
        Long currentUserId = 1L;
        CoupleUpdateRequest request = new CoupleUpdateRequest(
                "부산광역시",
                "빌라 웨딩",
                false,
                true,
                false,
                LocalDate.of(2025, 12, 25)
        );

        User currentUser = User.builder()
                .id(1L)
                .loginId("user123")
                .nickname("현재사용자")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        User partner = User.builder()
                .id(2L)
                .loginId("partner123")
                .nickname("파트너")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        Couple existingCouple = Couple.builder()
                .id(1L)
                .user1(currentUser)
                .user2(partner)
                .region("서울특별시")
                .weddingType("스몰 웨딩")
                .honeymoonBudget(true)
                .photoPackage(false)
                .dressMakeup(true)
                .weddingDate(LocalDate.of(2025, 8, 30))
                .createdAt(LocalDateTime.now())
                .build();

        Couple savedCouple = Couple.builder()
                .id(1L)
                .user1(currentUser)
                .user2(partner)
                .region("부산광역시")
                .weddingType("빌라 웨딩")
                .honeymoonBudget(false)
                .photoPackage(true)
                .dressMakeup(false)
                .weddingDate(LocalDate.of(2025, 12, 25))
                .createdAt(existingCouple.getCreatedAt())
                .build();

        given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentUser));
        given(coupleRepository.findByUserId(currentUserId)).willReturn(Optional.of(existingCouple));
        given(coupleRepository.save(any(Couple.class))).willReturn(savedCouple);

        // When
        CoupleRegisterResponse response = coupleService.updateCouple(request, currentUserId);

        // Then
        assertThat(response.getCoupleId()).isEqualTo(1L);
        verify(coupleRepository).save(any(Couple.class));
    }

    @Test
    @DisplayName("커플 정보 조회 성공")
    void getCoupleInfo_success() {
        // Given
        Long currentUserId = 1L;
        Long coupleId = 1L;

        User currentUser = User.builder()
                .id(1L)
                .loginId("user123")
                .nickname("현재사용자")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        User partner = User.builder()
                .id(2L)
                .loginId("partner123")
                .nickname("파트너")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        Couple couple = Couple.builder()
                .id(1L)
                .user1(currentUser)
                .user2(partner)
                .region("서울특별시")
                .weddingType("스몰 웨딩")
                .honeymoonBudget(true)
                .photoPackage(false)
                .dressMakeup(true)
                .weddingDate(LocalDate.of(2025, 8, 30))
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentUser));
        given(coupleRepository.findById(coupleId)).willReturn(Optional.of(couple));

        // When
        CoupleInfoResponse response = coupleService.getCoupleInfo(coupleId, currentUserId);

        // Then
        assertThat(response.getLoginId()).isEqualTo("partner123");
        assertThat(response.getRegion()).isEqualTo("서울특별시");
        assertThat(response.getWeddingType()).isEqualTo("스몰 웨딩");
        assertThat(response.getHoneymoonBudget()).isTrue();
        assertThat(response.getPhotoPackage()).isFalse();
        assertThat(response.getDressMakeup()).isTrue();
        assertThat(response.getWeddingDate()).isEqualTo(LocalDate.of(2025, 8, 30));
    }

    @Test
    @DisplayName("권한 없는 사용자가 커플 정보 조회 시 실패")
    void getCoupleInfo_fail_forbidden() {
        // Given
        Long currentUserId = 3L; // 커플에 속하지 않는 사용자
        Long coupleId = 1L;

        User currentUser = User.builder()
                .id(3L)
                .loginId("outsider123")
                .nickname("외부사용자")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        User user1 = User.builder()
                .id(1L)
                .loginId("user123")
                .nickname("사용자1")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        User user2 = User.builder()
                .id(2L)
                .loginId("partner123")
                .nickname("사용자2")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        Couple couple = Couple.builder()
                .id(1L)
                .user1(user1)
                .user2(user2)
                .region("서울특별시")
                .weddingType("스몰 웨딩")
                .honeymoonBudget(true)
                .photoPackage(false)
                .dressMakeup(true)
                .weddingDate(LocalDate.of(2025, 8, 30))
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentUser));
        given(coupleRepository.findById(coupleId)).willReturn(Optional.of(couple));

        // When & Then
        assertThatThrownBy(() -> coupleService.getCoupleInfo(coupleId, currentUserId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.FORBIDDEN);
    }
}