package com.weddingfit.dto.response.account;

import com.weddingfit.entity.account.Account;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(name = "AccountRegisterResponse", description = "계좌 등록 응답 DTO")
public class AccountRegisterResponse {

    @Schema(description = "계좌 ID", example = "1")
    private Long accountId;

    @Schema(description = "은행명", example = "KB국민은행")
    private String bankName;

    @Schema(description = "계좌번호 (마스킹 처리 권장)", example = "110-***-6789")
    private String accountNumber;

    @Schema(
            description = "외부 연동 Connected ID (코데프 등 제3자 API 연동 식별자)",
            example = "cid-20230908-xyz"
    )
    private String connectedId;

    public AccountRegisterResponse(Account account) {
        this.accountId = account.getAccountId();
        this.bankName = account.getBankName();
        this.accountNumber = account.getAccountNumber();
        this.connectedId = account.getConnectedId();
    }

    // 편의 메서드
    @Schema(description = "계좌가 외부 서비스와 연결되었는지 여부", example = "true")
    public boolean isConnected() {
        return connectedId != null && !connectedId.trim().isEmpty();
    }
}