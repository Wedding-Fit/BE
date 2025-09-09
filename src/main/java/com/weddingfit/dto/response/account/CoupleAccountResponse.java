package com.weddingfit.dto.response.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CoupleAccountResponse {
    
    @JsonProperty("accountInfo")
    private List<AccountInfo> accountInfo;
    
    @Getter
    @NoArgsConstructor
    public static class AccountInfo {
        @JsonProperty("accountName")
        private String accountName;
        
        @JsonProperty("accountNumber")
        private String accountNumber;
        
        public AccountInfo(String accountName, String accountNumber) {
            this.accountName = accountName;
            this.accountNumber = accountNumber;
        }
        
        public static AccountInfo createMasked(String bankName, String originalAccountNumber) {
            return new AccountInfo(bankName, maskAccountNumber(originalAccountNumber));
        }
        
        private static String maskAccountNumber(String accountNumber) {
            if (accountNumber == null || accountNumber.length() < 6) {
                return accountNumber;
            }
            
            String cleaned = accountNumber.replaceAll("-", "");
            if (cleaned.length() < 6) {
                return accountNumber;
            }
            
            String first3 = cleaned.substring(0, 3);
            String last3 = cleaned.substring(cleaned.length() - 3);
            String masked = first3 + "-***-***" + last3;
            
            return masked;
        }
    }
}