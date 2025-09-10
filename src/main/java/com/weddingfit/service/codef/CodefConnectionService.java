package com.weddingfit.service.codef;

import com.weddingfit.entity.account.Account;
import com.weddingfit.global.exception.AccountNotFoundException;
import com.weddingfit.global.exception.UnsupportedBankException;
import com.weddingfit.repository.account.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;

@Service
public class CodefConnectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CodefConnectionService.class);
    
    private final CodefApiService codefApiService;
    private final AccountRepository accountRepository;
    private final CodefTransactionService codefTransactionService;
    
    // 은행명 -> 은행코드 매핑
    private static final Map<String, String> BANK_CODE_MAP = new HashMap<>();
    
    static {
        BANK_CODE_MAP.put("국민은행", "0004");
        BANK_CODE_MAP.put("신한은행", "0088");
        BANK_CODE_MAP.put("우리은행", "0020");
        BANK_CODE_MAP.put("하나은행", "0081");
        BANK_CODE_MAP.put("농협은행", "0011");
        BANK_CODE_MAP.put("기업은행", "0003");
        BANK_CODE_MAP.put("SC제일은행", "0023");
        BANK_CODE_MAP.put("씨티은행", "0027");
        BANK_CODE_MAP.put("대구은행", "0031");
        BANK_CODE_MAP.put("부산은행", "0032");
        BANK_CODE_MAP.put("광주은행", "0034");
        BANK_CODE_MAP.put("제주은행", "0035");
        BANK_CODE_MAP.put("전북은행", "0037");
        BANK_CODE_MAP.put("경남은행", "0039");
        BANK_CODE_MAP.put("새마을금고", "0045");
        BANK_CODE_MAP.put("신협", "0048");
        BANK_CODE_MAP.put("우체국", "0071");
        BANK_CODE_MAP.put("KEB하나은행", "0081");
        BANK_CODE_MAP.put("카카오뱅크", "0090");
        BANK_CODE_MAP.put("케이뱅크", "0089");
        BANK_CODE_MAP.put("토스뱅크", "0092");
    }
    
    @Autowired
    public CodefConnectionService(CodefApiService codefApiService, AccountRepository accountRepository, 
                                 CodefTransactionService codefTransactionService) {
        this.codefApiService = codefApiService;
        this.accountRepository = accountRepository;
        this.codefTransactionService = codefTransactionService;
    }
    
    /**
     * 비동기로 계좌를 codef와 연동
     * @param accountId 연동할 계좌 ID
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void connectAccountAsync(Long accountId) {
        logger.info("백그라운드 codef 연동 시작: accountId={}", accountId);
        
        try {
            // 1. 계좌 정보 조회
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new AccountNotFoundException(accountId));
            
            if (account.isConnected()) {
                logger.warn("이미 연결된 계좌입니다: accountId={}, connectedId={}", accountId, account.getConnectedId());
                return;
            }
            
            // 2. 은행코드 매핑
            String bankCode = BANK_CODE_MAP.get(account.getBankName());
            if (bankCode == null) {
                throw new UnsupportedBankException(account.getBankName());
            }
            
            // 3. codef API 호출하여 connectId 발급
            CodefApiService.CodefConnectResponse response = codefApiService.registerAccount(
                    bankCode,
                    account.getBankId(),
                    account.getBankPassword()
            );
            
            // 4. 응답 확인 및 처리
            if (response != null && response.getResult() != null && "CF-00000".equals(response.getResult().getCode())) {
                // 성공 - data 안의 connectedId 사용
                String connectedId = response.getData() != null ? response.getData().getConnectedId() : null;
                if (connectedId != null) {
                    account.setConnectedId(connectedId);
                    accountRepository.save(account);
                    
                    logger.info("codef 연동 성공: accountId={}, connectedId={}", accountId, connectedId);
                    
                    // 5. 트랜잭션 커밋 후에 거래내역 동기화 실행
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            logger.info("트랜잭션 커밋 완료, 거래내역 동기화 시작: accountId={}", accountId);
                            codefTransactionService.syncAccountTransactions(accountId);
                        }
                    });
                } else {
                    logger.error("connectedId가 null입니다: accountId={}", accountId);
                }
                
            } else {
                // 실패
                String errorMessage = response != null && response.getResult() != null 
                        ? response.getResult().getMessage() 
                        : "알 수 없는 오류";
                
                logger.error("codef 연동 실패: accountId={}, error={}", accountId, errorMessage);
            }
            
        } catch (CodefTokenException e) {
            logger.error("codef 토큰 관련 오류 발생: accountId={}, message={}", accountId, e.getMessage());
            logger.info("토큰 문제로 인한 일시적 실패, 나중에 재시도 가능: accountId={}", accountId);
            
        } catch (Exception e) {
            logger.error("codef 연동 중 오류 발생: accountId={}", accountId, e);
        }
    }
    
    /**
     * 연동 대기 중인 모든 계좌를 처리 (스케줄러에서 호출)
     */
    @Transactional
    public void processPendingAccounts() {
        logger.info("연동 대기 중인 계좌들 처리 시작");
        
        var pendingAccounts = accountRepository.findByConnectedIdIsNull();
        logger.info("연동 대기 계좌 수: {}", pendingAccounts.size());
        
        for (Account account : pendingAccounts) {
            connectAccountAsync(account.getAccountId());
        }
    }
    
    /**
     * 연결된 계좌의 connectId 조회
     */
    public String getConnectedId(Long accountId) {
        return accountRepository.findById(accountId)
                .filter(Account::isConnected)
                .map(Account::getConnectedId)
                .orElse(null);
    }
    
    /**
     * 계좌 연동 상태 확인
     */
    public boolean isAccountConnected(Long accountId) {
        return accountRepository.findById(accountId)
                .map(Account::isConnected)
                .orElse(false);
    }
}