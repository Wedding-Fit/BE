package com.weddingfit.dto.response.goal;


import com.weddingfit.entity.deposit.DepositSavingType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long productId;
    private String companyName;
    private String productName;
    private DepositSavingType type;
    private BigDecimal interestRate;
    private BigDecimal monthlyPayment;
    private LocalDate estimatedMaturityDate;
    private BigDecimal estimatedAmount;
}
