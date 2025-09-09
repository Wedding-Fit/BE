package com.weddingfit.dto.request.account;

import jakarta.validation.constraints.NotBlank;

public class AccountRegisterRequest {
    
    @NotBlank(message = "은행명은 필수입니다")
    private String bankName;
    
    @NotBlank(message = "인터넷뱅킹 아이디는 필수입니다")
    private String bankId;
    
    @NotBlank(message = "인터넷뱅킹 비밀번호는 필수입니다")
    private String bankPassword;
    
    @NotBlank(message = "계좌번호는 필수입니다")
    private String accountNumber;
    
    private String accountPassword; // 선택적
    
    // 기본 생성자
    public AccountRegisterRequest() {}
    
    // 생성자
    public AccountRegisterRequest(String bankName, String bankId, String bankPassword, 
                                 String accountNumber, String accountPassword) {
        this.bankName = bankName;
        this.bankId = bankId;
        this.bankPassword = bankPassword;
        this.accountNumber = accountNumber;
        this.accountPassword = accountPassword;
    }
    
    // Getters (for service layer access)
    public String getBankName() { return bankName; }
    public String getBankId() { return bankId; }
    public String getBankPassword() { return bankPassword; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountPassword() { return accountPassword; }
}