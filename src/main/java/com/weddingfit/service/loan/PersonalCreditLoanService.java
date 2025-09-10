package com.weddingfit.service.loan;

import com.weddingfit.dto.response.loan.*;
import com.weddingfit.entity.loan.PersonalCreditLoan;
import com.weddingfit.repository.loan.PersonalCreditLoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalCreditLoanService {

    private final PersonalCreditLoanRepository repo;

    public PersonalCreditLoanListDataResponse getList(String sortStr, int page, int size) {
        Sort sort = parseSort(sortStr);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);
        Page<PersonalCreditLoan> result = repo.findAll(pageable);

        List<PersonalCreditLoanListItemResponse> rows = result.getContent().stream()
                .map(p -> PersonalCreditLoanListItemResponse.builder()
                        .personalCreditLoanId(p.getId())
                        .financialCompanyId(p.getCompany() != null ? p.getCompany().getId() : null)
                        .financialCompanyName(p.getCompany() != null ? p.getCompany().getName() : null)
                        .name(p.getName())
                        .crdtGradAvg(p.getCrdtGradAvg())
                        .build())
                .collect(Collectors.toList());

        return PersonalCreditLoanListDataResponse.builder()
                .personalCreditLoanList(rows)
                .page(result.getNumber())
                .size(result.getSize())
                .totalPages(result.getTotalPages())
                .totalCount(result.getTotalElements())
                .build();
    }

    public PersonalCreditLoanDetailItemResponse getDetail(Long id) {
        PersonalCreditLoan p = repo.findByIdFetchCompany(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 개인대출 상품입니다. id=" + id));

        return PersonalCreditLoanDetailItemResponse.builder()
                .personalCreditLoanId(p.getId())
                .financialCompanyId(p.getCompany() != null ? p.getCompany().getId() : null)
                .financialCompanyName(p.getCompany() != null ? p.getCompany().getName() : null)
                .name(p.getName())
                .joinWay(p.getJoinWay())
                .crdtGradAvg(p.getCrdtGradAvg())
                .contactNumber(p.getContactNumber())
                .productUrl(p.getProductUrl())
                .extraInfo(p.getExtraInfo())
                .build();
    }

    private Sort parseSort(String sortStr) {
        String prop = "crdtGradAvg";
        Sort.Direction dir = Sort.Direction.DESC;

        if (sortStr != null && !sortStr.isBlank()) {
            String[] t = sortStr.split(",", 2);
            if (t.length >= 1 && !t[0].isBlank()) {
                String f = t[0].trim();
                if (f.equals("crdtGradAvg") || f.equals("name")) prop = f;
            }
            if (t.length == 2 && !t[1].isBlank()) {
                String d = t[1].trim().toUpperCase();
                if (d.equals("ASC") || d.equals("DESC")) dir = Sort.Direction.fromString(d);
            }
        }
        return Sort.by(dir, prop);
    }
}

