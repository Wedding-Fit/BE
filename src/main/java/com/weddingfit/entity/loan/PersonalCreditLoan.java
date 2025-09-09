package com.weddingfit.entity.loan;

import com.weddingfit.entity.common.BaseTimeEntity;
import com.weddingfit.entity.company.FinancialCompany;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name="personal_credit_loan", indexes={
  @Index(name="idx_pcl_company_name", columnList="financial_company_id,name", unique=true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PersonalCreditLoan extends BaseTimeEntity{
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(name="personal_credit_loan_id") private Long id;

  @ManyToOne(fetch=FetchType.LAZY, optional=false)
  @JoinColumn(name="financial_company_id") private FinancialCompany company;

  @Column(nullable=false, length=200) private String name;
  @Column(name="join_way", length=200) private String joinWay;
  @Column(name="crdt_grad_avg", precision=10, scale=4) private BigDecimal crdtGradAvg;
  @Column(name="contact_number", length=100) private String contactNumber;
  @Column(name="product_url", length=500) private String productUrl;
  @Lob @Column(name="extra_info",columnDefinition = "LONGTEXT") private String extraInfo;
}
