package com.weddingfit.controller.couple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weddingfit.dto.request.couple.CoupleRegisterRequest;
import com.weddingfit.dto.response.couple.CoupleRegisterResponse;
import com.weddingfit.global.exception.CustomException;
import com.weddingfit.global.exception.GlobalErrorCode;
import com.weddingfit.global.security.JwtProvider;
import com.weddingfit.service.couple.CoupleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CoupleController.class)
class CoupleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CoupleService coupleService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("커플 등록 성공")
    void registerCouple_success() throws Exception {
        // Given
        CoupleRegisterRequest request = new CoupleRegisterRequest(
                "partner123",
                "서울특별시",
                "스몰 웨딩",
                true,
                false,
                true,
                LocalDate.of(2025, 8, 30)
        );

        CoupleRegisterResponse response = CoupleRegisterResponse.builder()
                .coupleId(1L)
                .build();

        String token = "valid-jwt-token";
        Long userId = 1L;

        given(jwtProvider.getUserId(token)).willReturn(userId);
        given(coupleService.registerCouple(any(CoupleRegisterRequest.class), eq(userId)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/couple/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("커플 등록에 성공했습니다"))
                .andExpect(jsonPath("$.data.coupleId").value(1L));
    }

    @Test
    @DisplayName("사용자를 찾을 수 없을 때 실패")
    void registerCouple_fail_user_not_found() throws Exception {
        // Given
        CoupleRegisterRequest request = new CoupleRegisterRequest(
                "partner123",
                "서울특별시",
                "스몰 웨딩",
                true,
                false,
                true,
                LocalDate.of(2025, 8, 30)
        );

        String token = "valid-jwt-token";
        Long userId = 1L;

        given(jwtProvider.getUserId(token)).willReturn(userId);
        given(coupleService.registerCouple(any(CoupleRegisterRequest.class), eq(userId)))
                .willThrow(new CustomException(GlobalErrorCode.USER_NOT_FOUND));

        // When & Then
        mockMvc.perform(post("/api/couple/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("본인과 커플 등록 시도 시 실패")
    void registerCouple_fail_cannot_couple_with_self() throws Exception {
        // Given
        CoupleRegisterRequest request = new CoupleRegisterRequest(
                "user123",
                "서울특별시",
                "스몰 웨딩",
                true,
                false,
                true,
                LocalDate.of(2025, 8, 30)
        );

        String token = "valid-jwt-token";
        Long userId = 1L;

        given(jwtProvider.getUserId(token)).willReturn(userId);
        given(coupleService.registerCouple(any(CoupleRegisterRequest.class), eq(userId)))
                .willThrow(new CustomException(GlobalErrorCode.CANNOT_COUPLE_WITH_SELF));

        // When & Then
        mockMvc.perform(post("/api/couple/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 커플이 존재할 때 등록 시도 시 실패")
    void registerCouple_fail_couple_already_exists() throws Exception {
        // Given
        CoupleRegisterRequest request = new CoupleRegisterRequest(
                "partner123",
                "서울특별시",
                "스몰 웨딩",
                true,
                false,
                true,
                LocalDate.of(2025, 8, 30)
        );

        String token = "valid-jwt-token";
        Long userId = 1L;

        given(jwtProvider.getUserId(token)).willReturn(userId);
        given(coupleService.registerCouple(any(CoupleRegisterRequest.class), eq(userId)))
                .willThrow(new CustomException(GlobalErrorCode.COUPLE_ALREADY_EXISTS));

        // When & Then
        mockMvc.perform(post("/api/couple/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}