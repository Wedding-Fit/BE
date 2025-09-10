package com.weddingfit.service.finlife;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weddingfit.entity.company.FinancialCompany;
import com.weddingfit.entity.deposit.DepositSaving;
import com.weddingfit.entity.deposit.DepositSavingType;
import com.weddingfit.entity.loan.JeonseLoan;
import com.weddingfit.entity.loan.PersonalCreditLoan;
import com.weddingfit.global.util.FinlifeJsonHelper;
import com.weddingfit.repository.company.FinancialCompanyRepository;
import com.weddingfit.repository.deposit.DepositSavingRepository;
import com.weddingfit.repository.loan.JeonseLoanRepository;
import com.weddingfit.repository.loan.PersonalCreditLoanRepository;
import java.math.BigDecimal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinlifeSyncService {

  private final FinlifeClient client;
  private final FinlifeJsonHelper M;
  private final ObjectMapper om = new ObjectMapper();

  private final FinancialCompanyRepository companyRepo;
  private final DepositSavingRepository depositSavingRepo;
  private final PersonalCreditLoanRepository pclRepo;
  private final JeonseLoanRepository jeonseRepo;

  // 예금/적금
  @Transactional
  public int syncDepositSaving(String path, DepositSavingType type) {
    int page = 1, saved = 0;
    while (true) {
      JsonNode root = client.get(path, page);
      JsonNode result = root != null ? root.get("result") : null;
      if (result == null) break;

      JsonNode baseList = result.get("baseList");
      JsonNode optionList = result.get("optionList");
      if (baseList == null || optionList == null) break;

      for (JsonNode opt : optionList) {
        String finCoNo = M.text(opt, "fin_co_no");
        String finPrdtCd = M.text(opt, "fin_prdt_cd");

        FinancialCompany c = upsertCompany(
            finCoNo,
            textFromBase(baseList, finPrdtCd, "kor_co_nm")
        );
        String productName = textFromBase(baseList, finPrdtCd, "fin_prdt_nm");

        String saveMonth = M.text(opt, "save_trm");
        BigDecimal intr = M.decimal(opt, "intr_rate");
        BigDecimal intr2 = M.decimal(opt, "intr_rate2");

        DepositSaving ds = depositSavingRepo
            .findByCompanyAndNameAndTypeAndSaveMonth(c, productName, type, saveMonth)
            .orElseGet(() -> DepositSaving.builder()
                .company(c).name(productName).type(type).saveMonth(saveMonth).build());

        ds.setInterestRate(intr);
        ds.setMaxInterestRate(intr2);

        JsonNode base = findBase(baseList, finPrdtCd);
        if (base != null) {
          ds.setEtcNote(M.text(base, "etc_note"));
          ds.setContactNumber(M.text(base, "cal_tel", "contact"));
          ds.setProductUrl(M.text(base, "dcls_url", "homp_url", "product_url"));
          ds.setExtraInfo(write(base));
        }

        depositSavingRepo.save(ds);
        saved++;
      }

      if (isLastPage(result, page)) break;
      page++;
    }
    return saved;
  }

  // 개인신용대출
  @Transactional
  public int syncPersonalCredit(String path) {
    int page = 1, saved = 0;
    while (true) {
      JsonNode root = client.get(path, page);
      JsonNode result = root != null ? root.get("result") : null;
      if (result == null) break;

      JsonNode baseList = result.get("baseList");
      if (baseList == null) break;

      // optionList에서 fin_prdt_cd별 A/B/C/D 평균을 만들어 맵핑
      Map<String, BigDecimal> avgMap = buildAvgMap(result.get("optionList"));

      for (JsonNode b : baseList) {
        FinancialCompany c = upsertCompany(M.text(b, "fin_co_no"), M.text(b, "kor_co_nm"));
        String name = M.text(b, "fin_prdt_nm");
        String prdtCd = M.text(b, "fin_prdt_cd");

        PersonalCreditLoan pcl = pclRepo.findByCompanyAndName(c, name)
            .orElseGet(() -> PersonalCreditLoan.builder().company(c).name(name).build());

        pcl.setJoinWay(M.text(b, "join_way"));

        // base의 crdt_grad_avg 우선, 없으면 optionList 산출값
        BigDecimal avgFromBase = M.decimal(b, "crdt_grad_avg");
        BigDecimal finalAvg = (avgFromBase != null) ? avgFromBase
            : (prdtCd != null ? avgMap.get(prdtCd) : null);
        pcl.setCrdtGradAvg(finalAvg);

        pcl.setContactNumber(M.text(b, "cal_tel", "contact"));
        pcl.setProductUrl(M.text(b, "dcls_url", "homp_url", "product_url"));
        pcl.setExtraInfo(write(b));

        pclRepo.save(pcl);
        saved++;
      }
      if (isLastPage(result, page)) break;
      page++;
    }
    return saved;
  }

  // 전세자금대출
  @Transactional
  public int syncJeonse(String path) {
    int page = 1, saved = 0;
    while (true) {
      JsonNode root = client.get(path, page);
      JsonNode result = root != null ? root.get("result") : null;
      if (result == null) break;

      JsonNode baseList = result.get("baseList");
      if (baseList == null) break;

      Map<String, RateAgg> rateMap = buildJeonseRateMap(result.get("optionList"));

      for (JsonNode b : baseList) {
        FinancialCompany c = upsertCompany(M.text(b, "fin_co_no"), M.text(b, "kor_co_nm"));
        String name = M.text(b, "fin_prdt_nm");
        String prdtCd = M.text(b, "fin_prdt_cd");

        JeonseLoan jl = jeonseRepo.findByCompanyAndName(c, name)
            .orElseGet(() -> JeonseLoan.builder().company(c).name(name).build());

        jl.setErlyFee(M.text(b, "erly_rpay_fee", "erly_fee"));
        jl.setDlyRate(M.text(b, "dly_rate"));
        jl.setLoanLmt(M.text(b, "loan_lmt"));

        RateAgg agg = (prdtCd != null) ? rateMap.get(prdtCd) : null;
        jl.setLendRateMin(agg != null ? agg.min : null);
        jl.setLendRateMax(agg != null ? agg.max : null);
        jl.setLendRateAvg(agg != null ? agg.avg : null);

        jl.setContactNumber(M.text(b, "cal_tel", "contact"));
        jl.setProductUrl(M.text(b, "dcls_url", "homp_url", "product_url"));
        jl.setExtraInfo(write(b));

        jeonseRepo.save(jl);
        saved++;
      }

      if (isLastPage(result, page)) break;
      page++;
    }
    return saved;
  }

  // 공통 helpers
  private FinancialCompany upsertCompany(String code, String name) {
    return companyRepo.findByCode(code)
        .map(c -> {
          if (name != null && !name.isBlank()) c.setName(name);
          return companyRepo.save(c);
        })
        .orElseGet(() -> companyRepo.save(FinancialCompany.builder().code(code).name(name).build()));
  }

  private JsonNode findBase(JsonNode baseList, String finPrdtCd) {
    if (baseList == null || !baseList.isArray()) return null;
    for (JsonNode b : baseList) {
      if (finPrdtCd != null && finPrdtCd.equals(M.text(b, "fin_prdt_cd"))) return b;
    }
    return null;
  }

  private String textFromBase(JsonNode baseList, String finPrdtCd, String key) {
    JsonNode b = findBase(baseList, finPrdtCd);
    return M.text(b, key);
  }

  private boolean isLastPage(JsonNode result, int curPage) {
    String now = M.text(result, "now_page_no", "nowPage", "pageNo");
    String max = M.text(result, "max_page_no", "maxPageNo", "totalPages", "maxPage");
    int cur = now != null ? Integer.parseInt(now) : curPage;
    int mx = max != null ? Integer.parseInt(max) : curPage;
    return cur >= mx;
  }

  public JsonNode rawCall(String path, int pageNo) {
    return client.get(path, pageNo);
  }

  private String write(JsonNode n) {
    try {
      return om.writeValueAsString(n);
    } catch (Exception e) {
      return null;
    }
  }

  // 개인신용 optionList 평균 계산
  private static BigDecimal toBD(JsonNode n) {
    if (n == null || n.isNull()) return null;
    try {
      return new BigDecimal(n.asText());
    } catch (Exception e) {
      return null;
    }
  }

  private static String txt(JsonNode n, String... keys) {
    if (n == null) return null;
    for (String k : keys) {
      if (n.has(k) && !n.get(k).isNull()) return n.get(k).asText();
    }
    return null;
  }

  private static class PAvg {
    BigDecimal A;
    BigDecimal B;
    BigDecimal C;
    BigDecimal D;
    BigDecimal best() {
      if (A != null) return A;
      BigDecimal sum = null;
      if (B != null) sum = (sum == null ? B : sum.add(B));
      if (C != null) sum = (sum == null ? C : sum.add(C));
      if (D != null) sum = (sum == null ? D : sum.add(D));
      return sum;
    }
  }

  private static Map<String, BigDecimal> buildAvgMap(JsonNode optionList) {
    Map<String, PAvg> temp = new HashMap<>();
    if (optionList == null || !optionList.isArray()) return Collections.emptyMap();

    for (JsonNode opt : optionList) {
      String prdtCd = txt(opt, "fin_prdt_cd");
      if (prdtCd == null) continue;

      String t = txt(opt, "crdt_lend_rate_type");
      BigDecimal avg = toBD(opt.get("crdt_grad_avg"));

      if (avg == null) {
        List<BigDecimal> vals = new ArrayList<>();
        Iterator<String> it = opt.fieldNames();
        while (it.hasNext()) {
          String k = it.next();
          if (k.startsWith("crdt_grad_") && !k.equals("crdt_grad_avg")) {
            BigDecimal v = toBD(opt.get(k));
            if (v != null) vals.add(v);
          }
        }
        if (!vals.isEmpty()) {
          BigDecimal sum = BigDecimal.ZERO;
          for (BigDecimal v : vals) sum = sum.add(v);
          avg = sum.divide(new BigDecimal(vals.size()), 4, java.math.RoundingMode.HALF_UP);
        }
      }
      if (avg == null) continue;

      PAvg p = temp.computeIfAbsent(prdtCd, k -> new PAvg());
      if ("A".equalsIgnoreCase(t)) p.A = avg;
      else if ("B".equalsIgnoreCase(t)) p.B = avg;
      else if ("C".equalsIgnoreCase(t)) p.C = avg;
      else if ("D".equalsIgnoreCase(t)) p.D = avg;
    }

    Map<String, BigDecimal> out = new HashMap<>();
    for (var e : temp.entrySet()) {
      BigDecimal v = e.getValue().best();
      if (v != null) out.put(e.getKey(), v.setScale(4, java.math.RoundingMode.HALF_UP));
    }
    return out;
  }

  // 전세 optionList 집계
  private static class RateAgg {
    BigDecimal min;
    BigDecimal max;
    BigDecimal avg;
    int score = Integer.MIN_VALUE;
  }

  private static int scoreOf(String rpayType, String lendRateType) {
    int r = "S".equalsIgnoreCase(rpayType) ? 2 : ("D".equalsIgnoreCase(rpayType) ? 1 : 0);
    int t = "C".equalsIgnoreCase(lendRateType) ? 2 : ("F".equalsIgnoreCase(lendRateType) ? 1 : 0);
    return r * 10 + t;
  }

  private static BigDecimal mid(BigDecimal a, BigDecimal b) {
    if (a == null || b == null) return null;
    return a.add(b).divide(new BigDecimal("2"), 4, java.math.RoundingMode.HALF_UP);
  }

  private Map<String, RateAgg> buildJeonseRateMap(JsonNode optionList) {
    Map<String, RateAgg> map = new HashMap<>();
    if (optionList == null || !optionList.isArray()) return map;

    for (JsonNode opt : optionList) {
      String prdtCd = M.text(opt, "fin_prdt_cd");
      if (prdtCd == null) continue;

      BigDecimal min = M.decimal(opt, "lend_rate_min");
      BigDecimal max = M.decimal(opt, "lend_rate_max");
      BigDecimal avg = M.decimal(opt, "lend_rate_avg");
      if (avg == null) avg = mid(min, max);

      String rpayType = M.text(opt, "rpay_type");
      String lendType = M.text(opt, "lend_rate_type");
      int score = scoreOf(rpayType, lendType);

      RateAgg agg = map.computeIfAbsent(prdtCd, k -> new RateAgg());

      if (min != null) agg.min = (agg.min == null || min.compareTo(agg.min) < 0) ? min : agg.min;
      if (max != null) agg.max = (agg.max == null || max.compareTo(agg.max) > 0) ? max : agg.max;

      if (avg != null && score > agg.score) {
        agg.avg = avg;
        agg.score = score;
      }
    }
    return map;
  }
}
