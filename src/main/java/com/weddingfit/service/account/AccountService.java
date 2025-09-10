package com.weddingfit.service.account;

import com.weddingfit.dto.request.account.AccountRegisterRequest;
import com.weddingfit.dto.response.account.AccountRegisterResponse;
import com.weddingfit.dto.response.account.CoupleAccountResponse;
import com.weddingfit.entity.account.Account;
import com.weddingfit.entity.couple.Couple;
import com.weddingfit.entity.user.User;
import com.weddingfit.repository.account.AccountRepository;
import com.weddingfit.repository.couple.CoupleRepository;
import com.weddingfit.repository.user.UserRepository;
import com.weddingfit.service.codef.CodefConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final CodefConnectionService codefConnectionService;
    
    @Autowired
    public AccountService(AccountRepository accountRepository, UserRepository userRepository,
                          CoupleRepository coupleRepository, CodefConnectionService codefConnectionService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.coupleRepository = coupleRepository;
        this.codefConnectionService = codefConnectionService;
    }
    
    /**
     * 계좌 등록
     * @param userId 사용자 ID (JWT에서 추출)
     * @param request 계좌 등록 요청
     * @return AccountRegisterResponse
     */
    @Transactional
    public AccountRegisterResponse registerAccount(Long userId, AccountRegisterRequest request) {
        logger.info("계좌 등록 요청: userId={}, bankName={}, accountNumber={}", 
                   userId, request.getBankName(), request.getAccountNumber());
        
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("사용자를 찾을 수 없습니다: userId={}", userId);
                    return new RuntimeException("사용자를 찾을 수 없습니다");
                });
        
        logger.debug("사용자 조회 성공: {}", user.getName());
        
        // 2. 중복 계좌 확인
        if (accountRepository.existsByUserAndAccountNumber(user, request.getAccountNumber())) {
            logger.warn("이미 등록된 계좌번호: {}", request.getAccountNumber());
            throw new RuntimeException("이미 등록된 계좌번호입니다");
        }
        
        // 3. 계좌 엔티티 생성 및 저장
        Account account = new Account(
                user,
                request.getBankId(),
                request.getBankPassword(),
                request.getBankName(),
                request.getAccountNumber(),
                request.getAccountPassword()
        );
        
        Account savedAccount = accountRepository.save(account);
        
        logger.info("계좌 DB 저장 완료: accountId={}, connectedId={}", 
                   savedAccount.getAccountId(), savedAccount.getConnectedId());
        
        // 4. 트랜잭션 커밋 후에 백그라운드에서 codef 연동 요청
        Long accountId = savedAccount.getAccountId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                logger.info("트랜잭션 커밋 완료, codef 비동기 연동 시작: accountId={}", accountId);
                codefConnectionService.connectAccountAsync(accountId);
            }
        });
        
        return new AccountRegisterResponse(savedAccount);
    }
    
    /**
     * 사용자의 등록된 계좌 목록 조회
     * @param userId 사용자 ID
     * @return 계좌 목록
     */
    public java.util.List<AccountRegisterResponse> getUserAccounts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        return accountRepository.findByUser(user)
                .stream()
                .map(AccountRegisterResponse::new)
                .toList();
    }
    
    /**
     * 커플의 계좌 목록 조회
     * @param userId 요청한 사용자 ID (권한 검증용)
     * @param coupleId 조회할 커플 ID
     * @return 커플 계좌 응답
     */
    public CoupleAccountResponse getCoupleAccounts(Long userId, Long coupleId) {
        logger.info("커플 계좌 조회 요청: userId={}, coupleId={}", userId, coupleId);
        
        // 1. 커플 조회
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> {
                    logger.error("존재하지 않는 커플: coupleId={}", coupleId);
                    return new IllegalArgumentException("존재하지 않는 커플입니다");
                });
        
        // 2. 권한 검증 - 요청한 사용자가 해당 커플의 멤버인지 확인
        if (!couple.getUser1().getId().equals(userId) && !couple.getUser2().getId().equals(userId)) {
            logger.warn("계좌 조회 권한 없음: userId={}, coupleId={}", userId, coupleId);
            throw new SecurityException("계좌 정보를 조회할 권한이 없습니다");
        }
        
        // 3. 커플 두 명의 계좌 모두 조회
        java.util.List<Account> user1Accounts = accountRepository.findByUser(couple.getUser1());
        java.util.List<Account> user2Accounts = accountRepository.findByUser(couple.getUser2());
        
        // 4. 계좌 정보 합치기
        java.util.List<CoupleAccountResponse.AccountInfo> accountInfos = new java.util.ArrayList<>();
        
        user1Accounts.forEach(account -> 
            accountInfos.add(CoupleAccountResponse.AccountInfo.createMasked(
                account.getBankName(), 
                account.getAccountNumber()
            ))
        );
        
        user2Accounts.forEach(account -> 
            accountInfos.add(CoupleAccountResponse.AccountInfo.createMasked(
                account.getBankName(), 
                account.getAccountNumber()
            ))
        );
        
        logger.info("커플 계좌 조회 성공: coupleId={}, 총 계좌 수={}", coupleId, accountInfos.size());
        
        return new CoupleAccountResponse(accountInfos);
    }
}