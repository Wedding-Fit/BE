package com.weddingfit.dto.response.account;

import com.weddingfit.entity.account.Account;

public class AccountRegisterResponse {
    
    private Long accountId;
    private String bankName;
    private String accountNumber;
    private String connectedId;
    
    // 기본 생성자
    public AccountRegisterResponse() {}
    
    // Account 엔티티로부터 생성하는 생성자
    public AccountRegisterResponse(Account account) {
        this.accountId = account.getAccountId();
        this.bankName = account.getBankName();
        this.accountNumber = account.getAccountNumber();
        this.connectedId = account.getConnectedId();
    }
    
    // Getters
    public Long getAccountId() { return accountId; }
    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public String getConnectedId() { return connectedId; }
    
    // 편의 메서드
    public boolean isConnected() { return connectedId != null && !connectedId.trim().isEmpty(); }
}