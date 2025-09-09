package com.weddingfit.entity.loan;

import com.weddingfit.entity.common.BaseTimeEntity;
import com.weddingfit.entity.company.FinancialCompany;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name="jeonse_loan", indexes={
  @Index(name="idx_jeonse_company_name", columnList="financial_company_id,name", unique=true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JeonseLoan extends BaseTimeEntity {
      @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(name="jeonse_loan_id") private Long id;

  @ManyToOne(fetch=FetchType.LAZY, optional=false)
  @JoinColumn(name="financial_company_id") private FinancialCompany company;

  @Column(nullable=false, length=200) private String name;
  @Column(name="erly_fee", length=200) private String erlyFee;
  @Column(name="dly_rate", length=200) private String dlyRate;
  @Column(name="loan_lmt", length=200) private String loanLmt;
  @Column(name="lend_rate_min", precision=10, scale=4) private BigDecimal lendRateMin;
  @Column(name="lend_rate_max", precision=10, scale=4) private BigDecimal lendRateMax;
  @Column(name="lend_rate_avg", precision=10, scale=4) private BigDecimal lendRateAvg;
  @Column(name="contact_number", length=100) private String contactNumber;
  @Column(name="product_url", length=500) private String productUrl;
  @Lob @Column(name="extra_info", columnDefinition = "LONGTEXT") private String extraInfo;
}
