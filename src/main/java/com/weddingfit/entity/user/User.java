package com.weddingfit.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @Column(name="login_id", nullable = false, length = 50, unique = true)
    private String loginId;

    @Column(name="password", nullable = false, length = 255)
    private String password;

    @Column(name="name", nullable = false, length = 50)
    private String name;

    @Column(name="nickname", length = 50, unique = true)
    private String nickname;

    @Column(name = "birth")
    private LocalDate birth;                   // 생년월일 (DATE ↔ LocalDate)

    public enum Gender { MALE, FEMALE }

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", columnDefinition = "ENUM('MALE','FEMALE')")
    private Gender gender; // 성별 (MySQL ENUM 매핑)

    @Column(name = "phone_number", length = 20, unique = true)
    private String phoneNumber;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 활성 상태 (기본값 true)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성일시

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
