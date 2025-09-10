package com.weddingfit.service.codef;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weddingfit.entity.account.Account;
import com.weddingfit.entity.transaction.Transaction;
import com.weddingfit.entity.user.User;
import com.weddingfit.repository.account.AccountRepository;
import com.weddingfit.repository.transaction.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodefTransactionService 테스트")
class CodefTransactionServiceTest {

    @Mock
    private CodefApiService codefApiService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CodefTransactionService codefTransactionService;

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
        testAccount.setConnectedId("connected-id-123");
    }

    @Test
    @DisplayName("거래내역 동기화 성공 테스트")
    void syncAccountTransactions_Success() {
        // Given
        Long accountId = 1L;
        CodefApiService.CodefTransactionResponse mockResponse = createMockTransactionResponse();
        List<Transaction> existingTransactions = new ArrayList<>();

        given(accountRepository.findById(accountId)).willReturn(Optional.of(testAccount));
        given(codefApiService.getTransactionHistory(anyString(), anyString(), anyString(), anyString(), anyString()))
                .willReturn(mockResponse);
        given(transactionRepository.findByAccount(testAccount)).willReturn(existingTransactions);
        given(transactionRepository.saveAll(any(List.class))).willReturn(new ArrayList<>());

        // Mock ObjectMapper and JsonNode
        JsonNode mockJsonNode = createMockJsonNode();
        when(objectMapper.valueToTree(any())).thenReturn(mockJsonNode);

        // When
        codefTransactionService.syncAccountTransactions(accountId);

        // Then
        verify(accountRepository).findById(accountId);
        verify(codefApiService).getTransactionHistory(
                eq("connected-id-123"),
                eq("110-123-456789"),
                eq("0004"),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("거래내역 동기화 실패 테스트 - 연결되지 않은 계좌")
    void syncAccountTransactions_NotConnectedAccount() {
        // Given
        Long accountId = 1L;
        testAccount.setConnectedId(null);

        given(accountRepository.findById(accountId)).willReturn(Optional.of(testAccount));

        // When
        codefTransactionService.syncAccountTransactions(accountId);

        // Then
        verify(accountRepository).findById(accountId);
        verify(codefApiService, org.mockito.Mockito.never())
                .getTransactionHistory(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("거래내역 동기화 실패 테스트 - 계좌를 찾을 수 없음")
    void syncAccountTransactions_AccountNotFound() {
        // Given
        Long accountId = 999L;

        given(accountRepository.findById(accountId)).willReturn(Optional.empty());

        // When & Then - 예외가 발생해야 함
        try {
            codefTransactionService.syncAccountTransactions(accountId);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("계좌를 찾을 수 없습니다");
        }

        verify(accountRepository).findById(accountId);
    }

    @Test
    @DisplayName("거래내역 동기화 실패 테스트 - API 응답 오류")
    void syncAccountTransactions_ApiError() {
        // Given
        Long accountId = 1L;
        CodefApiService.CodefTransactionResponse errorResponse = createErrorResponse();

        given(accountRepository.findById(accountId)).willReturn(Optional.of(testAccount));
        given(codefApiService.getTransactionHistory(anyString(), anyString(), anyString(), anyString(), anyString()))
                .willReturn(errorResponse);

        // When
        codefTransactionService.syncAccountTransactions(accountId);

        // Then
        verify(accountRepository).findById(accountId);
        verify(codefApiService).getTransactionHistory(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(transactionRepository, org.mockito.Mockito.never()).saveAll(any());
    }

    @Test
    @DisplayName("모든 연결된 계좌 동기화 테스트")
    void syncAllConnectedAccounts_Success() {
        // Given
        Account account1 = new Account(testUser, "user1", "pass1", "국민은행", "111-111-111111", "1111");
        account1.setAccountId(1L);
        account1.setConnectedId("connected-1");

        Account account2 = new Account(testUser, "user2", "pass2", "신한은행", "222-222-222222", "2222");
        account2.setAccountId(2L);
        account2.setConnectedId("connected-2");

        List<Account> connectedAccounts = List.of(account1, account2);

        given(accountRepository.findByConnectedIdIsNotNull()).willReturn(connectedAccounts);

        // When
        codefTransactionService.syncAllConnectedAccounts();

        // Then
        verify(accountRepository).findByConnectedIdIsNotNull();
    }

    @Test
    @DisplayName("거래내역 카테고리 분류 테스트 - 식비")
    void categorizeTransaction_Food() throws Exception {
        // Given
        String description = "스타벅스강남점";

        // When (private 메소드를 리플렉션으로 호출)
        java.lang.reflect.Method method = codefTransactionService.getClass()
                .getDeclaredMethod("categorizeTransaction", String.class);
        method.setAccessible(true);
        Transaction.TransactionCategory result = (Transaction.TransactionCategory) method.invoke(codefTransactionService, description);

        // Then
        assertThat(result).isEqualTo(Transaction.TransactionCategory.FOOD);
    }

    // Helper methods
    private CodefApiService.CodefTransactionResponse createMockTransactionResponse() {
        CodefApiService.CodefTransactionResponse response = new CodefApiService.CodefTransactionResponse();
        
        CodefApiService.CodefTransactionResponse.Result result = 
                new CodefApiService.CodefTransactionResponse.Result();
        result.setCode("CF-00000");
        result.setMessage("정상");
        result.setTransactionId("tx-123");
        response.setResult(result);

        CodefApiService.CodefTransactionResponse.TransactionData data = 
                new CodefApiService.CodefTransactionResponse.TransactionData();
        data.setResAccountBalance("1000000");
        data.setResAccountDisplay("110-***-***789");
        data.setResAccountName("입출금통장");
        data.setResAccountHolder("테스트유저");
        
        // Mock transaction list
        Object[] transactionList = new Object[1];
        transactionList[0] = new Object(); // Will be mocked by ObjectMapper
        data.setResTrHistoryList(transactionList);
        
        response.setData(data);
        return response;
    }

    private CodefApiService.CodefTransactionResponse createErrorResponse() {
        CodefApiService.CodefTransactionResponse response = new CodefApiService.CodefTransactionResponse();
        
        CodefApiService.CodefTransactionResponse.Result result = 
                new CodefApiService.CodefTransactionResponse.Result();
        result.setCode("CF-03001");
        result.setMessage("등록되지 않은 connectedId입니다");
        result.setTransactionId("tx-error");
        response.setResult(result);

        return response;
    }

    private JsonNode createMockJsonNode() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonString = "{\n" +
                    "  \"resAccountTrDate\": \"20240830\",\n" +
                    "  \"resAccountTrTime\": \"143022\",\n" +
                    "  \"resAccountDesc3\": \"스타벅스강남점\",\n" +
                    "  \"resAccountOut\": \"5500\",\n" +
                    "  \"resAccountIn\": \"0\",\n" +
                    "  \"resAfterTranBalance\": \"994500\"\n" +
                    "}";
            return mapper.readTree(jsonString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}