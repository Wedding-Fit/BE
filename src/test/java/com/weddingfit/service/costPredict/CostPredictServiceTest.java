package com.weddingfit.service.costPredict;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.weddingfit.dto.response.costPredict.CostPredictDetailResponse;
import com.weddingfit.dto.response.costPredict.CostPredictResponse;
import com.weddingfit.entity.costPredict.CostPredict;
import com.weddingfit.entity.couple.Couple;
import com.weddingfit.repository.costPredict.CostPredictRepository;
import com.weddingfit.repository.couple.CoupleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CostPredictServiceTest {

    @Mock private CostPredictRepository repository;
    @Mock private CoupleRepository coupleRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OpenAIClient openAI;

    private CostPredictService service;

    @BeforeEach
    void setUp() {
        service = new CostPredictService(repository, coupleRepository, openAI);
        when(openAI.chat().completions().create(any(ChatCompletionCreateParams.class)))
                .thenThrow(new RuntimeException("boom"));
    }

    private Couple buildCouple(Long id) {
        Couple c = mock(Couple.class);
        when(c.getRegion()).thenReturn("서울특별시");
        when(c.getWeddingType()).thenReturn("호텔 웨딩");
        when(c.getHoneymoonBudget()).thenReturn(Boolean.TRUE);
        when(c.getPhotoPackage()).thenReturn(Boolean.TRUE);
        when(c.getDressMakeup()).thenReturn(Boolean.TRUE);
        when(c.getWeddingDate()).thenReturn(LocalDate.of(2025, 10, 10));
        when(c.getTotalAmount()).thenReturn(BigDecimal.valueOf(30_000_000L));
        return c;
    }

    private static String computeFingerprint(
            String region, String weddingType,
            boolean hb, boolean photo, boolean dm,
            LocalDate date, long planned
    ) {
        String payload = String.join("|",
                region != null ? region : "",
                weddingType != null ? weddingType : "",
                hb ? "1" : "0",
                photo ? "1" : "0",
                dm ? "1" : "0",
                date != null ? date.toString() : "",
                String.valueOf(planned)
        );
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return payload;
        }
    }

    private CostPredict buildEntity(Long coupleId, long ceremony, long food, long studio, long dress, long honey,
                                    long total, String fingerprint, String snapshotJson) {
        return CostPredict.builder()
                .coupleId(coupleId)
                .ceremonyCost(BigDecimal.valueOf(ceremony))
                .foodCost(BigDecimal.valueOf(food))
                .studioCost(BigDecimal.valueOf(studio))
                .dressCost(BigDecimal.valueOf(dress))
                .honeymoonBudgetCost(BigDecimal.valueOf(honey))
                .totalCost(BigDecimal.valueOf(total))
                .profileFingerprint(fingerprint)
                .profileSnapshot(snapshotJson)
                .build();
    }

    @Nested
    class NewComputeAndSave {

        @Test
        @DisplayName("fallback 기반 계산이 수행되고 저장된다")
        void computeAndUpsert_withFallback_whenNoExisting() {
            Long coupleId = 7L;
            Couple couple = buildCouple(coupleId);
            when(coupleRepository.findById(coupleId)).thenReturn(Optional.of(couple));
            when(repository.findTopByCoupleIdOrderByPredictedAtDesc(coupleId)).thenReturn(Optional.empty());
            when(repository.findTopByCoupleIdOrderByPredictionIdDesc(coupleId)).thenReturn(Optional.empty());

            ArgumentCaptor<CostPredict> savedCaptor = ArgumentCaptor.forClass(CostPredict.class);
            when(repository.save(savedCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            CostPredictDetailResponse detail = service.getDetail(coupleId);

            verify(repository, times(1)).hardDeleteAllByCoupleId(coupleId);
            verify(repository, times(1)).save(any(CostPredict.class));

            CostPredict saved = savedCaptor.getValue();
            long total = saved.getTotalCost().longValue();
            long sumParts = saved.getCeremonyCost().longValue()
                    + saved.getFoodCost().longValue()
                    + saved.getStudioCost().longValue()
                    + saved.getDressCost().longValue()
                    + saved.getHoneymoonBudgetCost().longValue();

            assertThat(total).isPositive();
            assertThat(sumParts).isEqualTo(total);
            assertThat(detail.getTotalCost()).isEqualTo(total);
            assertThat(detail.getCeremonyCost()
                    + detail.getFoodCost()
                    + detail.getStudioCost()
                    + detail.getDressCost()
                    + detail.getHoneymoonBudgetCost())
                    .isEqualTo(detail.getTotalCost());
        }
    }

    @Nested
    class ReuseExistingWhenFingerprintSame {

        @Test
        @DisplayName("fingerprint 일치시 재계산/삭제/저장 안 함")
        void reuseExisting_ifFingerprintMatches() {
            Long coupleId = 9L;
            Couple couple = buildCouple(coupleId);
            when(coupleRepository.findById(coupleId)).thenReturn(Optional.of(couple));

            String fp = computeFingerprint(
                    "서울특별시", "호텔 웨딩",
                    true, true, true,
                    LocalDate.of(2025, 10, 10),
                    30_000_000L
            );

            long total = 50_000_000L;
            CostPredict existing = buildEntity(
                    coupleId,
                    18_000_000L,
                    15_000_000L,
                    10_000_000L,
                    5_000_000L,
                    2_000_000L,
                    total,
                    fp,
                    "{\"region\":\"서울특별시\"}"
            );

            when(repository.findTopByCoupleIdOrderByPredictedAtDesc(coupleId)).thenReturn(Optional.of(existing));

            CostPredictDetailResponse detail = service.getDetail(coupleId);
            CostPredictResponse ratio = service.getRatios(coupleId);

            verify(repository, never()).hardDeleteAllByCoupleId(anyLong());
            verify(repository, never()).save(any());

            assertThat(detail.getTotalCost()).isEqualTo(total);
            long sum = detail.getCeremonyCost() + detail.getFoodCost()
                    + detail.getStudioCost() + detail.getDressCost()
                    + detail.getHoneymoonBudgetCost();
            assertThat(sum).isEqualTo(total);

            int rsum = ratio.getCeremonyRatio() + ratio.getFoodRatio()
                    + ratio.getStudioRatio() + ratio.getDressRatio()
                    + ratio.getHoneymoonBudgetRatio();
            assertThat(rsum).isEqualTo(100);
        }
    }

    @Test
    @DisplayName("getRatios만 호출해도 내부적으로 ensureUpToDate가 동작한다")
    void getRatios_only() {
        Long coupleId = 11L;
        Couple couple = buildCouple(coupleId);
        when(coupleRepository.findById(coupleId)).thenReturn(Optional.of(couple));
        when(repository.findTopByCoupleIdOrderByPredictedAtDesc(coupleId)).thenReturn(Optional.empty());
        when(repository.findTopByCoupleIdOrderByPredictionIdDesc(coupleId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CostPredictResponse ratios = service.getRatios(coupleId);

        verify(repository, times(1)).hardDeleteAllByCoupleId(coupleId);
        verify(repository, times(1)).save(any());

        int sumRatio = ratios.getCeremonyRatio() + ratios.getFoodRatio()
                + ratios.getStudioRatio() + ratios.getDressRatio()
                + ratios.getHoneymoonBudgetRatio();
        assertThat(sumRatio).isEqualTo(100);
    }
}
