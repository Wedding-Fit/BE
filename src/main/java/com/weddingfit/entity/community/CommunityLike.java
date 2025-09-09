package com.weddingfit.entity.community;

import com.weddingfit.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "community_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_community_like_community_user",
                        columnNames = {"community_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_community_like_community", columnList = "community_id"),
                @Index(name = "idx_community_like_user", columnList = "user_id")
        }
)
public class CommunityLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "community_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_like_community")
    )
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_like_user")
    )
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
