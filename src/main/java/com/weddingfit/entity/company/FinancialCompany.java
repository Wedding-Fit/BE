package com.weddingfit.entity.company;

import com.weddingfit.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="financial_company", indexes={
  @Index(name="idx_fin_company_code", columnList="code", unique=true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinancialCompany extends BaseTimeEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(name="financial_company_id")
  private Long id;

  @Column(length=64, nullable=false, unique=true) private String code; // fin_co_no
  @Column(length=200, nullable=false) private String name;            // kor_co_nm
}
