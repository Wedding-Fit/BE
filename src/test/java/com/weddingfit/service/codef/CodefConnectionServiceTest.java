package com.weddingfit.service.codef;

import com.weddingfit.entity.account.Account;
import com.weddingfit.entity.user.User;
import com.weddingfit.repository.account.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodefConnectionService 테스트")
class CodefConnectionServiceTest {

    @Mock
    private CodefApiService codefApiService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CodefTransactionService codefTransactionService;

    @InjectMocks
    private CodefConnectionService codefConnectionService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("테스트유저")
                .loginId("testuser123")
                .password("testpass")
                .nickname("테스트닉네임")
                .build();

        testAccount = new Account(testUser, "test123", "password", "국민은행", 
                                "110-123-456789", "1234");
        testAccount.setAccountId(1L);
    }

    @Test
    @DisplayName("계좌 연동 성공 테스트")
    void connectAccountAsync_Success() {
        // Given
        Long accountId = 1L;
        String expectedConnectedId = "connected-id-123";

        CodefApiService.CodefConnectResponse successResponse = createSuccessResponse(expectedConnectedId);

        given(accountRepository.findById(accountId)).willReturn(Optional.of(testAccount));
        given(codefApiService.registerAccount(anyString(), anyString(), anyString()))
                .willReturn(successResponse);
        given(accountRepository.save(any(Account.class))).willReturn(testAccount);

        // When
        codefConnectionService.connectAccountAsync(accountId);

        // Then
        verify(accountRepository).findById(accountId);
        verify(codefApiService).registerAccount("0004", "test123", "password");
        verify(accountRepository).save(testAccount);
        assertThat(testAccount.getConnectedId()).isEqualTo(expectedConnectedId);
    }

    @Test
    @DisplayName("계좌 연동 실패 테스트 - 이미 연결된 계좌")
    void connectAccountAsync_AlreadyConnected() {
        // Given
        Long accountId = 1L;
        testAccount.setConnectedId("already-connected-id");

        given(accountRepository.findById(accountId)).willReturn(Optional.of(testAccount));

        // When
        codefConnectionService.connectAccountAsync(accountId);

        // Then
        verify(accountRepository).findById(accountId);
        verify(codefApiService, never()).registerAccount(anyString(), anyString(), anyString());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("계좌 연동 실패 테스트 - 지원하지 않는 은행")
    void connectAccountAsync_UnsupportedBank() {
        // Given
        Long accountId = 1L;
        Account unsupportedBankAccount = new Account(testUser, "test123", "password", 
                "테스트은행", "110-123-456789", "1234");
        unsupportedBankAccount.setAccountId(accountId);

        given(accountRepository.findById(accountId)).willReturn(Optional.of(unsupportedBankAccount));

        // When
        codefConnectionService.connectAccountAsync(accountId);

        // Then
        verify(accountRepository).findById(accountId);
        verify(codefApiService, never()).registerAccount(anyString(), anyString(), anyString());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("계좌 연동 실패 테스트 - Codef API 오류")
    void connectAccountAsync_CodefApiError() {
        // Given
        Long accountId = 1L;
        CodefApiService.CodefConnectResponse errorResponse = createErrorResponse("CF-03002", "로그인 실패");

        given(accountRepository.findById(accountId)).willReturn(Optional.of(testAccount));
        given(codefApiService.registerAccount(anyString(), anyString(), anyString()))
                .willReturn(errorResponse);

        // When
        codefConnectionService.connectAccountAsync(accountId);

        // Then
        verify(accountRepository).findById(accountId);
        verify(codefApiService).registerAccount("0004", "test123", "password");
        verify(accountRepository, never()).save(any());
        assertThat(testAccount.getConnectedId()).isNull();
    }

    @Test
    @DisplayName("계좌를 찾을 수 없는 경우 테스트")
    void connectAccountAsync_AccountNotFound() {
        // Given
        Long accountId = 999L;

        given(accountRepository.findById(accountId)).willReturn(Optional.empty());

        // When & Then
        codefConnectionService.connectAccountAsync(accountId);

        verify(accountRepository).findById(accountId);
        verify(codefApiService, never()).registerAccount(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("연동 대기 중인 계좌들 처리 테스트")
    void processPendingAccounts_Success() {
        // Given
        Account pendingAccount1 = new Account(testUser, "user1", "pass1", "국민은행", "111-111-111111", "1111");
        pendingAccount1.setAccountId(1L);
        
        Account pendingAccount2 = new Account(testUser, "user2", "pass2", "신한은행", "222-222-222222", "2222");
        pendingAccount2.setAccountId(2L);

        List<Account> pendingAccounts = Arrays.asList(pendingAccount1, pendingAccount2);

        given(accountRepository.findByConnectedIdIsNull()).willReturn(pendingAccounts);

        // When
        codefConnectionService.processPendingAccounts();

        // Then
        verify(accountRepository).findByConnectedIdIsNull();
    }

    @Test
    @DisplayName("연결된 계좌의 connectedId 조회 테스트")
    void getConnectedId_Success() {
        // Given
        Long accountId = 1L;
        String expectedConnectedId = "connected-id-123";
        testAccount.setConnectedId(expectedConnectedId);

        given(accountRepository.findById(accountId)).willReturn(Optional.of(testAccount));

        // When
        String result = codefConnectionService.getConnectedId(accountId);

        // Then
        assertThat(result).isEqualTo(expectedConnectedId);
        verify(accountRepository).findById(accountId);
    }

    @Test
    @DisplayName("연결되지 않은 계좌의 connectedId 조회 테스트")
    void getConnectedId_NotConnected() {
        // Given
        Long accountId = 1L;

        given(accountRepository.findById(accountId)).willReturn(Optional.of(testAccount));

        // When
        String result = codefConnectionService.getConnectedId(accountId);

        // Then
        assertThat(result).isNull();
        verify(accountRepository).findById(accountId);
    }

    @Test
    @DisplayName("계좌 연동 상태 확인 테스트")
    void isAccountConnected_True() {
        // Given
        Long accountId = 1L;
        testAccount.setConnectedId("connected-id-123");

        given(accountRepository.findById(accountId)).willReturn(Optional.of(testAccount));

        // When
        boolean result = codefConnectionService.isAccountConnected(accountId);

        // Then
        assertThat(result).isTrue();
        verify(accountRepository).findById(accountId);
    }

    @Test
    @DisplayName("계좌 연동 상태 확인 테스트 - 연결되지 않은 계좌")
    void isAccountConnected_False() {
        // Given
        Long accountId = 1L;

        given(accountRepository.findById(accountId)).willReturn(Optional.of(testAccount));

        // When
        boolean result = codefConnectionService.isAccountConnected(accountId);

        // Then
        assertThat(result).isFalse();
        verify(accountRepository).findById(accountId);
    }

    // Helper methods
    private CodefApiService.CodefConnectResponse createSuccessResponse(String connectedId) {
        CodefApiService.CodefConnectResponse response = new CodefApiService.CodefConnectResponse();
        
        CodefApiService.CodefConnectResponse.Result result = new CodefApiService.CodefConnectResponse.Result();
        result.setCode("CF-00000");
        result.setMessage("정상");
        result.setTransactionId("tx-123");
        response.setResult(result);

        CodefApiService.CodefConnectResponse.Data data = new CodefApiService.CodefConnectResponse.Data();
        data.setConnectedId(connectedId);
        response.setData(data);

        return response;
    }

    private CodefApiService.CodefConnectResponse createErrorResponse(String errorCode, String errorMessage) {
        CodefApiService.CodefConnectResponse response = new CodefApiService.CodefConnectResponse();
        
        CodefApiService.CodefConnectResponse.Result result = new CodefApiService.CodefConnectResponse.Result();
        result.setCode(errorCode);
        result.setMessage(errorMessage);
        result.setTransactionId("tx-error");
        response.setResult(result);

        return response;
    }
}