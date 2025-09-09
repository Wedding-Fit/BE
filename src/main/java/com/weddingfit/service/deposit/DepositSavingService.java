package com.weddingfit.service.deposit;

import com.weddingfit.dto.response.deposit.*;
import com.weddingfit.entity.deposit.DepositSaving;
import com.weddingfit.entity.deposit.DepositSavingType;
import com.weddingfit.repository.deposit.DepositSavingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepositSavingService {
    private final DepositSavingRepository depositSavingRepository;

    public DepositSavingListDataResponse getDepositList(String sortStr, int page, int size) {
        return getListByType(DepositSavingType.DEPOSIT, sortStr, page, size);
    }

    public DepositSavingDetailItemResponse getDepositDetail(Long id) {
        return getDetail(id);
    }

    public DepositSavingListDataResponse getSavingList(String sortStr, int page, int size) {
        return getListByType(DepositSavingType.SAVING, sortStr, page, size);
    }

    public DepositSavingDetailItemResponse getSavingDetail(Long id) {
        return getDetail(id);
    }

    // 공통 내부 로직
    private DepositSavingListDataResponse getListByType(DepositSavingType type, String sortStr, int page, int size) {
        Sort sort = parseSort(sortStr);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);

        Page<DepositSaving> result = depositSavingRepository.findByType(type, pageable);

        List<DepositSavingListItemResponse> rows = result.getContent().stream()
                .map(ds -> DepositSavingListItemResponse.builder()
                        .depositSavingId(ds.getId())
                        .financialCompanyId(ds.getCompany() != null ? ds.getCompany().getId() : null)
                        .financialCompanyName(ds.getCompany() != null ? ds.getCompany().getName() : null)
                        .type(ds.getType())
                        .name(ds.getName())
                        .interestRate(ds.getInterestRate())
                        .maxInterestRate(ds.getMaxInterestRate())
                        .build())
                .collect(Collectors.toList());

        return DepositSavingListDataResponse.builder()
                .depositSavingList(rows)
                .page(result.getNumber())
                .size(result.getSize())
                .totalPages(result.getTotalPages())
                .totalCount(result.getTotalElements())
                .build();
    }

    private DepositSavingDetailItemResponse getDetail(Long id) {
        DepositSaving ds = depositSavingRepository.findByIdFetchCompany(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. id=" + id));

        return DepositSavingDetailItemResponse.builder()
                .depositSavingId(ds.getId())
                .financialCompanyId(ds.getCompany() != null ? ds.getCompany().getId() : null)
                .financialCompanyName(ds.getCompany() != null ? ds.getCompany().getName() : null)
                .type(ds.getType())
                .name(ds.getName())
                .interestRate(ds.getInterestRate())
                .maxInterestRate(ds.getMaxInterestRate())
                .saveMonth(ds.getSaveMonth())
                .etcNote(ds.getEtcNote())
                .contactNumber(ds.getContactNumber())
                .productUrl(ds.getProductUrl())
                .extraInfo(ds.getExtraInfo())
                .build();
    }

    private Sort parseSort(String sortStr) {
        String prop = "maxInterestRate";
        Sort.Direction dir = Sort.Direction.DESC;

        if (sortStr != null && !sortStr.isBlank()) {
            String[] t = sortStr.split(",", 2);
            if (t.length >= 1 && !t[0].isBlank()) {
                String f = t[0].trim();
                if (f.equals("maxInterestRate") || f.equals("interestRate")) prop = f;
            }
            if (t.length == 2 && !t[1].isBlank()) {
                String d = t[1].trim().toUpperCase();
                if (d.equals("ASC") || d.equals("DESC")) dir = Sort.Direction.fromString(d);
            }
        }
        return Sort.by(dir, prop);
    }
}
