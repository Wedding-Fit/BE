package com.weddingfit.repository.analytics;

import com.weddingfit.entity.analytics.SpendingAnalytics;
import com.weddingfit.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpendingAnalyticsRepository extends JpaRepository<SpendingAnalytics, Long> {
    
    @Query("SELECT sa FROM SpendingAnalytics sa WHERE sa.user = :user ORDER BY sa.analysisDate DESC LIMIT 1")
    Optional<SpendingAnalytics> findLatestByUser(@Param("user") User user);
    
    @Query("SELECT sa FROM SpendingAnalytics sa WHERE sa.user = :user AND sa.analysisDate = :date")
    List<SpendingAnalytics> findByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
}