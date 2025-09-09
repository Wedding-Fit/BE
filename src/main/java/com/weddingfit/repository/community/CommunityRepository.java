package com.weddingfit.repository.community;

import com.weddingfit.entity.community.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    List<Community> findByCategory(Community.Category category);
}
