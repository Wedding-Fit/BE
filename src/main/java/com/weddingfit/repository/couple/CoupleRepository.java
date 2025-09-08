package com.weddingfit.repository.couple;

import com.weddingfit.entity.couple.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoupleRepository extends JpaRepository<Couple, Long> {

    @Query("select count(c) > 0 from Couple c " +
            "where c.user1.id = :userId or c.user2.id = :userId")
    boolean existsByMember(@Param("userId") Long userId);

    @Query("select c from Couple c where c.user1.id = :userId or c.user2.id = :userId")
    Optional<Couple> findByUserId(@Param("userId") Long userId);
}