package com.weddingfit.repository.account;

import com.weddingfit.entity.account.Account;
import com.weddingfit.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    // 사용자별 계좌 조회
    List<Account> findByUser(User user);
    
    // 사용자별 연결된 계좌만 조회
    List<Account> findByUserAndConnectedIdIsNotNull(User user);
    
    // 연동 대기 중인 계좌들 조회
    List<Account> findByConnectedIdIsNull();
    
    // 특정 사용자의 특정 계좌번호 중복 확인
    boolean existsByUserAndAccountNumber(User user, String accountNumber);
    
    // connectedId로 계좌 찾기
    Optional<Account> findByConnectedId(String connectedId);
}