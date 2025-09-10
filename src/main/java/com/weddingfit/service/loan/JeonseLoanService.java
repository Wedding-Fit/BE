package com.weddingfit.service.loan;

import com.weddingfit.dto.response.loan.*;
import com.weddingfit.entity.loan.JeonseLoan;
import com.weddingfit.repository.loan.JeonseLoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JeonseLoanService {

    private final JeonseLoanRepository repo;

    public JeonseLoanListDataResponse getList(String sortStr, int page, int size) {
        Sort sort = parseSort(sortStr);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);
        Page<JeonseLoan> result = repo.findAll(pageable);

        List<JeonseLoanListItemResponse> rows = result.getContent().stream()
                .map(j -> JeonseLoanListItemResponse.builder()
                        .jeonseLoanId(j.getId())
                        .financialCompanyId(j.getCompany() != null ? j.getCompany().getId() : null)
                        .financialCompanyName(j.getCompany() != null ? j.getCompany().getName() : null)
                        .name(j.getName())
                        .lendRateMin(j.getLendRateMin())
                        .lendRateMax(j.getLendRateMax())
                        .lendRateAvg(j.getLendRateAvg())
                        .build())
                .collect(Collectors.toList());

        return JeonseLoanListDataResponse.builder()
                .jeonseLoanList(rows)
                .page(result.getNumber())
                .size(result.getSize())
                .totalPages(result.getTotalPages())
                .totalCount(result.getTotalElements())
                .build();
    }

    public JeonseLoanDetailItemResponse getDetail(Long id) {
        JeonseLoan j = repo.findByIdFetchCompany(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전세대출 상품입니다. id=" + id));

        return JeonseLoanDetailItemResponse.builder()
                .jeonseLoanId(j.getId())
                .financialCompanyId(j.getCompany() != null ? j.getCompany().getId() : null)
                .financialCompanyName(j.getCompany() != null ? j.getCompany().getName() : null)
                .name(j.getName())
                .erlyFee(j.getErlyFee())
                .dlyRate(j.getDlyRate())
                .loanLmt(j.getLoanLmt())
                .lendRateMin(j.getLendRateMin())
                .lendRateMax(j.getLendRateMax())
                .lendRateAvg(j.getLendRateAvg())
                .contactNumber(j.getContactNumber())
                .productUrl(j.getProductUrl())
                .extraInfo(j.getExtraInfo())
                .build();
    }

    private Sort parseSort(String sortStr) {
        String prop = "lendRateAvg"; // 기본 평균금리 높은 순
        Sort.Direction dir = Sort.Direction.DESC;

        if (sortStr != null && !sortStr.isBlank()) {
            String[] t = sortStr.split(",", 2);
            if (t.length >= 1 && !t[0].isBlank()) {
                String f = t[0].trim();
                if (f.equals("lendRateAvg") || f.equals("lendRateMin") || f.equals("lendRateMax")) prop = f;
            }
            if (t.length == 2 && !t[1].isBlank()) {
                String d = t[1].trim().toUpperCase();
                if (d.equals("ASC") || d.equals("DESC")) dir = Sort.Direction.fromString(d);
            }
        }
        return Sort.by(dir, prop);
    }
}

