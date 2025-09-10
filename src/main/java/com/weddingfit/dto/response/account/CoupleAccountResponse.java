package com.weddingfit.dto.response.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CoupleAccountResponse", description = "커플 계좌 목록 응답 DTO")
public class CoupleAccountResponse {

    @JsonProperty("accountInfo")
    @Schema(description = "계좌 정보 리스트")
    private List<AccountInfo> accountInfo;

    @Getter
    @NoArgsConstructor
    @Schema(name = "AccountInfo", description = "개별 계좌 정보")
    public static class AccountInfo {

        @JsonProperty("accountName")
        @Schema(description = "계좌 이름 (은행명 등)", example = "KB국민은행")
        private String accountName;

        @JsonProperty("accountNumber")
        @Schema(description = "계좌번호 (마스킹 처리된 값)", example = "110-***-***789")
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