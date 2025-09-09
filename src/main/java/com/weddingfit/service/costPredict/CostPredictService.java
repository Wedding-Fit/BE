package com.weddingfit.service.costPredict;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.weddingfit.dto.response.costPredict.CostPredictDetailResponse;
import com.weddingfit.dto.response.costPredict.CostPredictResponse;
import com.weddingfit.entity.costPredict.CostPredict;
import com.weddingfit.entity.couple.Couple;
import com.weddingfit.repository.costPredict.CostPredictRepository;
import com.weddingfit.repository.couple.CoupleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CostPredictService {

    private final CostPredictRepository repository;
    private final CoupleRepository coupleRepository;
    private final OpenAIClient openAI;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Set<String> ALLOWED_WEDDING_TYPES = Set.of(
            "스몰 웨딩", "하객 중심", "야외 웨딩", "호텔 웨딩", "셀프 웨딩", "전통 혼례"
    );
    private static final Set<String> ALLOWED_REGIONS = Set.of(
            "서울특별시", "경기도", "강원도", "광주광역시", "대구광역시", "부산광역시", "대전광역시", "울산광역시"
    );

    private static BigDecimal money(long v) { return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP); }
    private static BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    @Transactional
    public CostPredictResponse getRatios(Long coupleId) {
        return ensureUpToDate(coupleId).ratio;
    }

    @Transactional
    public CostPredictDetailResponse getDetail(Long coupleId) {
        return ensureUpToDate(coupleId).detail;
    }

    private Pair ensureUpToDate(Long coupleId) {
        Features f   = loadFeatures(coupleId);
        String fpNow = fingerprintOf(f);

        Optional<CostPredict> latest = findLatest(coupleId);
        if (latest.isEmpty()) {
            return computeAndUpsert(coupleId, f, fpNow);
        }

        CostPredict e = latest.get();
        String fpSaved = e.getProfileFingerprint();
        if (fpSaved == null || !fpSaved.equals(fpNow)) {
            return computeAndUpsert(coupleId, f, fpNow);
        }

        return new Pair(toRatioFromEntity(e), toDetailResponse(e));
    }

    private Optional<CostPredict> findLatest(Long coupleId) {
        Optional<CostPredict> byTime = repository.findTopByCoupleIdOrderByPredictedAtDesc(coupleId);
        return byTime.isPresent() ? byTime : repository.findTopByCoupleIdOrderByPredictionIdDesc(coupleId);
    }

    @Transactional
    protected Pair computeAndUpsert(Long coupleId, Features f, String fingerprint) {
        AiSuggestion ai = suggestByGpt(f);

        int ceremonyRatio = clamp(ai.ceremonyRatio(), 0, 100);
        int foodRatio     = clamp(ai.foodRatio(), 0, 100);
        int studioRatio   = clamp(ai.studioRatio(), 0, 100);
        int dressRatio    = clamp(ai.dressRatio(), 0, 100);
        int honeyRatio    = clamp(ai.honeymoonBudgetRatio(), 0, 100);

        int sum = ceremonyRatio + foodRatio + studioRatio + dressRatio + honeyRatio;
        if (sum != 100) honeyRatio = clamp(honeyRatio + (100 - sum), 0, 100);

        long total = Math.max(0, ai.totalBudget());
        Split split = splitByRatios(total, ceremonyRatio, foodRatio, studioRatio, dressRatio, honeyRatio);

        repository.hardDeleteAllByCoupleId(coupleId);

        CostPredict entity = CostPredict.builder()
                .coupleId(coupleId)
                .ceremonyCost(money(split.ceremony))
                .foodCost(money(split.food))
                .studioCost(money(split.studio))
                .dressCost(money(split.dress))
                .honeymoonBudgetCost(money(split.honeymoon))
                .totalCost(money(total))
                .profileFingerprint(fingerprint)
                .profileSnapshot(buildSnapshotJson(f))
                .build();

        repository.save(entity);
        return new Pair(toRatioFromEntity(entity), toDetailResponse(entity));
    }

    private AiSuggestion suggestByGpt(Features f) {
        AiSuggestion fallback = fallbackFor(f);

        try {
            String system = """
                You are a wedding cost analyst for South Korea.
                Use the couple's profile to estimate a realistic total wedding budget in KRW
                and allocation ratios across ceremony, food, studio, dress, honeymoonBudget.

                IMPORTANT:
                - Output JSON only (no markdown/text).
                - Keys: ceremonyRatio, foodRatio, studioRatio, dressRatio, honeymoonBudgetRatio (integers summing to 100),
                        totalBudget (positive integer KRW).
                - Consider region, wedding type, options, and seasonality (peak months May/Oct higher).
                - Allowed weddingTypes: ["스몰 웨딩","하객 중심","야외 웨딩","호텔 웨딩","셀프 웨딩","전통 혼례"]
                - Allowed regions: ["서울특별시","경기도","강원도","광주광역시","대구광역시","부산광역시","대전광역시","울산광역시"]
                """;

            String user = """
                Couple Profile:
                - region: %s   (use as-is if allowed, otherwise treat as 기타)
                - weddingType: %s   (use as-is if allowed, otherwise treat as 기타)
                - honeymoonBudget(planned?): %s
                - photoPackage(studio shoot): %s
                - dressMakeup: %s
                - weddingDate(YYYY-MM): %s
                - userPlannedBudgetKRW(optional hint): %s

                Constraints:
                - Return ONLY JSON:
                  {"ceremonyRatio":int,"foodRatio":int,"studioRatio":int,"dressRatio":int,"honeymoonBudgetRatio":int,"totalBudget":int}
                - Ratios must sum to 100; totalBudget must be a positive integer in KRW (e.g., 25000000).
                """.formatted(
                    f.region, f.weddingType,
                    f.honeymoonBudget ? "true" : "false",
                    f.photoPackage ? "true" : "false",
                    f.dressMakeup ? "true" : "false",
                    f.weddingDate != null ? f.weddingDate.format(DateTimeFormatter.ofPattern("yyyy-MM")) : "unknown",
                    f.userPlannedBudget > 0 ? String.valueOf(f.userPlannedBudget) : "unknown"
            );

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4O_MINI)
                    .addSystemMessage(system)
                    .addUserMessage(user)
                    .temperature(0.2)
                    .build();

            ChatCompletion res = openAI.chat().completions().create(params);
            String raw = res.choices().get(0).message().content().orElse("");
            String json = extractJson(raw);

            JsonNode n = MAPPER.readTree(json);
            return new AiSuggestion(
                    n.path("ceremonyRatio").asInt(fallback.ceremonyRatio()),
                    n.path("foodRatio").asInt(fallback.foodRatio()),
                    n.path("studioRatio").asInt(fallback.studioRatio()),
                    n.path("dressRatio").asInt(fallback.dressRatio()),
                    n.path("honeymoonBudgetRatio").asInt(fallback.honeymoonBudgetRatio()),
                    n.path("totalBudget").asLong(fallback.totalBudget())
            );
        } catch (Exception e) {
            return fallback;
        }
    }

    private Features loadFeatures(Long coupleId) {
        Couple c = coupleRepository.findById(coupleId).orElse(null);
        if (c == null) return new Features("기타", "기타", false, false, false, null, 0L);

        String region = normalizeRegion(c.getRegion());
        String weddingType = normalizeWeddingType(c.getWeddingType());
        boolean honeymoonBudget = Boolean.TRUE.equals(c.getHoneymoonBudget());
        boolean photoPackage    = Boolean.TRUE.equals(c.getPhotoPackage());
        boolean dressMakeup     = Boolean.TRUE.equals(c.getDressMakeup());
        LocalDate weddingDate   = c.getWeddingDate();
        long plannedBudget      = toLongSafe(c.getTotalAmount());

        return new Features(region, weddingType, honeymoonBudget, photoPackage, dressMakeup, weddingDate, plannedBudget);
    }

    private String normalizeRegion(String input) {
        if (input == null) return "기타";
        String s = input.trim();
        return ALLOWED_REGIONS.contains(s) ? s : "기타";
    }

    private String normalizeWeddingType(String input) {
        if (input == null) return "기타";
        String s = input.trim();
        return ALLOWED_WEDDING_TYPES.contains(s) ? s : "기타";
    }

    private String fingerprintOf(Features f) {
        // 안정적 직렬화(정규화된 값 사용)
        String payload = String.join("|",
                nullToBlank(f.region),
                nullToBlank(f.weddingType),
                f.honeymoonBudget ? "1" : "0",
                f.photoPackage ? "1" : "0",
                f.dressMakeup ? "1" : "0",
                f.weddingDate != null ? f.weddingDate.toString() : "",
                String.valueOf(f.userPlannedBudget)
        );
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return payload;
        }
    }

    private String buildSnapshotJson(Features f) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("region", f.region);
        n.put("weddingType", f.weddingType);
        n.put("honeymoonBudget", f.honeymoonBudget);
        n.put("photoPackage", f.photoPackage);
        n.put("dressMakeup", f.dressMakeup);
        n.put("weddingDate", f.weddingDate != null ? f.weddingDate.toString() : null);
        n.put("userPlannedBudget", f.userPlannedBudget);
        return n.toString();
    }

    private static String nullToBlank(String s) { return s == null ? "" : s; }

    private AiSuggestion fallbackFor(Features f) {
        long base = f.userPlannedBudget > 0 ? f.userPlannedBudget : 20_000_000L;

        double regionMul = switch (f.region) {
            case "서울특별시" -> 1.25;
            case "경기도"     -> 1.15;
            case "부산광역시", "광주광역시", "대구광역시", "대전광역시", "울산광역시" -> 1.10;
            case "강원도"     -> 0.95;
            default           -> 1.00;
        };

        double typeMul = switch (f.weddingType) {
            case "호텔 웨딩" -> 1.30;
            case "야외 웨딩" -> 1.15;
            case "하객 중심" -> 1.10;
            case "전통 혼례" -> 1.05;
            case "스몰 웨딩" -> 0.80;
            case "셀프 웨딩" -> 0.70;
            default          -> 1.00;
        };

        double seasonMul = 1.00;
        if (f.weddingDate != null) {
            int m = f.weddingDate.getMonthValue();
            if (m == 5 || m == 10) seasonMul = 1.15;
            else if (m == 4 || m == 9 || m == 11) seasonMul = 1.10;
        }

        long estimated = Math.round(base * regionMul * typeMul * seasonMul);

        if (f.honeymoonBudget) estimated += Math.round(estimated * 0.05);
        if (f.photoPackage)    estimated += Math.round(estimated * 0.05);
        if (f.dressMakeup)     estimated += Math.round(estimated * 0.05);

        return new AiSuggestion(35, 30, 25, 7, 3, Math.max(estimated, base));
    }

    private CostPredictResponse toRatioFromEntity(CostPredict e) {
        BigDecimal total = nvl(e.getTotalCost());
        if (total.signum() <= 0) {
            return CostPredictResponse.builder()
                    .ceremonyRatio(20).foodRatio(20).studioRatio(20).dressRatio(20).honeymoonBudgetRatio(20)
                    .build();
        }
        int ceremony = percent(e.getCeremonyCost(), total);
        int food = percent(e.getFoodCost(), total);
        int studio = percent(e.getStudioCost(), total);
        int dress = percent(e.getDressCost(), total);
        int honey = 100 - (ceremony + food + studio + dress);

        return CostPredictResponse.builder()
                .ceremonyRatio(ceremony)
                .foodRatio(food)
                .studioRatio(studio)
                .dressRatio(dress)
                .honeymoonBudgetRatio(honey)
                .build();
    }

    private CostPredictDetailResponse toDetailResponse(CostPredict e) {
        CostPredictResponse ratio = toRatioFromEntity(e);

        return CostPredictDetailResponse.builder()
                .ceremonyRatio(ratio.getCeremonyRatio())
                .foodRatio(ratio.getFoodRatio())
                .studioRatio(ratio.getStudioRatio())
                .dressRatio(ratio.getDressRatio())
                .honeymoonBudgetRatio(ratio.getHoneymoonBudgetRatio())
                .ceremonyCost(toLong(e.getCeremonyCost()))
                .foodCost(toLong(e.getFoodCost()))
                .studioCost(toLong(e.getStudioCost()))
                .dressCost(toLong(e.getDressCost()))
                .honeymoonBudgetCost(toLong(e.getHoneymoonBudgetCost()))
                .totalCost(toLong(e.getTotalCost()))
                .build();
    }

    private Split splitByRatios(long total, int ceremonyRatio, int foodRatio, int studioRatio, int dressRatio, int honeymoonRatio) {
        long c = Math.round(total * (ceremonyRatio / 100.0));
        long f = Math.round(total * (foodRatio / 100.0));
        long s = Math.round(total * (studioRatio / 100.0));
        long d = Math.round(total * (dressRatio / 100.0));
        long h = Math.round(total * (honeymoonRatio / 100.0));
        long diff = total - (c + f + s + d + h);
        h += diff; // 총액 보존
        return new Split(c, f, s, d, h);
    }

    private static int percent(BigDecimal part, BigDecimal total) {
        if (part == null || total == null || total.signum() == 0) return 0;
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private static long toLong(BigDecimal v) { return nvl(v).setScale(0, RoundingMode.HALF_UP).longValueExact(); }
    private static long toLongSafe(BigDecimal v) { return v == null ? 0L : v.setScale(0, RoundingMode.HALF_UP).longValue(); }
    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private static String extractJson(String raw) {
        int s = raw.indexOf('{'); int e = raw.lastIndexOf('}');
        return (s >= 0 && e >= s) ? raw.substring(s, e + 1) : raw;
    }

    private record Features(String region, String weddingType,
                            boolean honeymoonBudget, boolean photoPackage, boolean dressMakeup,
                            LocalDate weddingDate, long userPlannedBudget) {}
    private record AiSuggestion(int ceremonyRatio, int foodRatio, int studioRatio, int dressRatio,
                                int honeymoonBudgetRatio, long totalBudget) {}
    private record Pair(CostPredictResponse ratio, CostPredictDetailResponse detail) {}
    private record Split(long ceremony, long food, long studio, long dress, long honeymoon) {}
}
