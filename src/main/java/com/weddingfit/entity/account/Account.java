package com.weddingfit.entity.account;

import com.weddingfit.entity.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; //

    @Column(name = "bank_id", nullable = false)
    private String bankId; // (예: "0004")

    @Column(name = "bank_password", nullable = false)
    private String bankPassword;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "account_password")
    private String accountPassword;

    @Column(name = "connected_id", length = 100)
    private String connectedId; // codef connectedId

    @Column(name = "balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ===== constructors =====
    protected Account() { } // JPA 기본 생성자

    public Account(User user, String bankId, String bankPassword, String bankName,
                   String accountNumber, String accountPassword) {
        this.user = user;
        this.bankId = bankId;
        this.bankPassword = bankPassword;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountPassword = accountPassword;
        this.balance = BigDecimal.ZERO;
    }

    // getters/setters
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getBankId() { return bankId; }
    public void setBankId(String bankId) { this.bankId = bankId; }

    public String getBankPassword() { return bankPassword; }
    public void setBankPassword(String bankPassword) { this.bankPassword = bankPassword; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountPassword() { return accountPassword; }
    public void setAccountPassword(String accountPassword) { this.accountPassword = accountPassword; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getConnectedId() { return connectedId; }
    public void setConnectedId(String connectedId) { this.connectedId = connectedId; }

    // 편의 메서드
    public boolean isConnected() {
        return connectedId != null && !connectedId.isBlank();
    }
}