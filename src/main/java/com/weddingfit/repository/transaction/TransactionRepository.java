package com.weddingfit.repository.transaction;

import com.weddingfit.entity.account.Account;
import com.weddingfit.entity.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // 특정 계좌의 거래내역 조회
    List<Transaction> findByAccount(Account account);
    
    // 특정 계좌의 기간별 거래내역 조회
    @Query("SELECT t FROM Transaction t WHERE t.account = :account " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountAndDateBetween(@Param("account") Account account,
                                                 @Param("startDate") java.sql.Date startDate,
                                                 @Param("endDate") java.sql.Date endDate);
    
    // 사용자별 전체 거래내역 조회
    @Query("SELECT t FROM Transaction t WHERE t.account.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserAndDateBetween(@Param("userId") Long userId,
                                              @Param("startDate") java.sql.Date startDate,
                                              @Param("endDate") java.sql.Date endDate);
    
    // 여러 계좌의 특정 기간 및 거래 유형별 조회 (소비 분석용)
    @Query("SELECT t FROM Transaction t WHERE t.account IN :accounts " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.transactionType = :transactionType " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountInAndTransactionDateBetweenAndTransactionType(
            @Param("accounts") List<Account> accounts,
            @Param("startDate") java.sql.Date startDate,
            @Param("endDate") java.sql.Date endDate,
            @Param("transactionType") Transaction.TransactionType transactionType);
}