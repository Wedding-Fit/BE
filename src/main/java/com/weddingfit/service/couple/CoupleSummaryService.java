package com.weddingfit.service.couple;

import com.weddingfit.dto.response.couple.CoupleSummaryResponse;
import com.weddingfit.entity.couple.Couple;
import com.weddingfit.repository.couple.CoupleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoupleSummaryService {

    private final CoupleRepository coupleRepository;

    public CoupleSummaryResponse getSummary(Long coupleId) {
        Couple c = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new IllegalArgumentException("invalid coupleId"));

        // dDay: 미래 음수 / 당일 0 / 과거 양수
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        int dDay = (int) ChronoUnit.DAYS.between(c.getWeddingDate(), today);

        String g1 = safe(toStringOrNull(c.getUser1() != null ? c.getUser1().getGender() : null)); // "MALE"/"FEMALE"/null
        String g2 = safe(toStringOrNull(c.getUser2() != null ? c.getUser2().getGender() : null));

        String maleName;
        String femaleName;
        String u1Name = c.getUser1() != null ? c.getUser1().getName() : "";
        String u2Name = c.getUser2() != null ? c.getUser2().getName() : "";

        if ("MALE".equals(g1) && "FEMALE".equals(g2)) {
            maleName = u1Name;
            femaleName = u2Name;
        } else if ("FEMALE".equals(g1) && "MALE".equals(g2)) {
            maleName = u2Name;
            femaleName = u1Name;
        } else {
            maleName = u1Name;
            femaleName = u2Name;
        }

        BigDecimal totalAmount = c.getTotalAmount() != null ? c.getTotalAmount() : BigDecimal.ZERO;

        return CoupleSummaryResponse.builder()
                .dDay(dDay)
                .maleName(maleName)
                .femaleName(femaleName)
                .totalAmount(totalAmount)
                .build();
    }

    private String toStringOrNull(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private String safe(String s) {
        return s == null ? null : s.trim().toUpperCase();
    }
}
