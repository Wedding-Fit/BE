package com.weddingfit.entity.deposit;

import com.weddingfit.entity.common.BaseTimeEntity;
import com.weddingfit.entity.company.FinancialCompany;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name="deposit_saving", indexes={
  @Index(name="uniq_ds_company_name_type_month",
         columnList="financial_company_id,name,type,save_month", unique=true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepositSaving extends BaseTimeEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(name="deposit_saving_id") private Long id;

  @ManyToOne(fetch=FetchType.LAZY, optional=false)
  @JoinColumn(name="financial_company_id") private FinancialCompany company;

  @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
  private DepositSavingType type;

  @Column(nullable=false, length=200) private String name;
  @Column(name="etc_note", length=500) private String etcNote;
  @Column(name="save_month", length=32) private String saveMonth;
  @Column(name="interest_rate", precision=10, scale=4) private BigDecimal interestRate;
  @Column(name="max_interest_rate", precision=10, scale=4) private BigDecimal maxInterestRate;
  @Column(name="contact_number", length=100) private String contactNumber;
  @Column(name="product_url", length=500) private String productUrl;
  @Lob @Column(name="extra_info", columnDefinition = "LONGTEXT") private String extraInfo;
}