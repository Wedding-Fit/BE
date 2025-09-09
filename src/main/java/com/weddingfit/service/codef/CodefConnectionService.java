package com.weddingfit.service.codef;

import com.weddingfit.entity.account.Account;
import com.weddingfit.repository.account.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class CodefConnectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CodefConnectionService.class);
    
    private final CodefApiService codefApiService;
    private final AccountRepository accountRepository;
    
    // 은행명 -> 은행코드 매핑
    private static final Map<String, String> BANK_CODE_MAP = new HashMap<>();
    
    static {
        BANK_CODE_MAP.put("국민은행", "0004");
        BANK_CODE_MAP.put("신한은행", "0088");
        BANK_CODE_MAP.put("우리은행", "0020");
        BANK_CODE_MAP.put("하나은행", "0081");
        BANK_CODE_MAP.put("농협은행", "0011");
        BANK_CODE_MAP.put("기업은행", "0003");
        // 필요에 따라 추가
    }
    
    @Autowired
    public CodefConnectionService(CodefApiService codefApiService, AccountRepository accountRepository) {
        this.codefApiService = codefApiService;
        this.accountRepository = accountRepository;
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
                    .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + accountId));
            
            if (account.isConnected()) {
                logger.warn("이미 연결된 계좌입니다: accountId={}, connectedId={}", accountId, account.getConnectedId());
                return;
            }
            
            // 2. 은행코드 매핑
            String bankCode = BANK_CODE_MAP.get(account.getBankName());
            if (bankCode == null) {
                logger.error("지원하지 않는 은행입니다: {}", account.getBankName());
                return;
            }
            
            // 3. codef API 호출하여 connectId 발급
            CodefApiService.CodefConnectResponse response = codefApiService.registerAccount(
                    bankCode,
                    account.getBankId(),
                    account.getBankPassword()
            );
            
            // 4. 응답 확인 및 처리
            if (response != null && response.getResult() != null && "CF-00000".equals(response.getResult().getCode())) {
                // 성공
                account.setConnectedId(response.getConnectedId());
                accountRepository.save(account);
                
                logger.info("codef 연동 성공: accountId={}, connectedId={}", accountId, response.getConnectedId());
                
                // 5. 초기 거래내역 동기화 (선택사항)
                // syncInitialTransactions(account);
                
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