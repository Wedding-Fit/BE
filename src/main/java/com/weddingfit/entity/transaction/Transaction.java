package com.weddingfit.entity.transaction;

import com.weddingfit.entity.account.Account;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;
    
    @Column(name = "transaction_date")
    private java.sql.Date transactionDate;
    
    @Column(name = "transaction_time")
    private String transactionTime;
    
    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private TransactionCategory category; // 소비패턴 분석용 카테고리
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum TransactionType {
        INCOME,         // 수입
        EXPENSE         // 지출
    }
    
    public enum TransactionCategory {
        FOOD,           // 식비
        CULTURE,        // 문화생활
        MEDICAL,        // 의료비
        TRANSPORT,      // 교통비
        SHOPPING,       // 쇼핑
        EDU,            // 교육비
        TELECOM,        // 통신비
        ETC             // 기타
    }
    
    // 기본 생성자
    public Transaction() {}
    
    // 생성자
    public Transaction(Account account, java.sql.Date transactionDate, String description, BigDecimal balanceAfter) {
        this.account = account;
        this.transactionDate = transactionDate;
        this.description = description;
        this.balanceAfter = balanceAfter;
        this.category = TransactionCategory.ETC; // 기본값
    }
    
    // Getters & Setters  
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    
    public java.sql.Date getTransactionDate() { return transactionDate; }
    public void setTransactionDate(java.sql.Date transactionDate) { this.transactionDate = transactionDate; }
    
    public String getTransactionTime() { return transactionTime; }
    public void setTransactionTime(String transactionTime) { this.transactionTime = transactionTime; }
    
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public TransactionCategory getCategory() { return category; }
    public void setCategory(TransactionCategory category) { this.category = category; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}