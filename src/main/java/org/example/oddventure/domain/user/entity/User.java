package org.example.oddventure.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oddventure.common.entity.BaseEntity;
import org.example.oddventure.domain.user.enums.UserRole;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Column(nullable = false)
    private BigDecimal point;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Builder
    public User(String username, String email, String password, UserRole userRole) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.point = new BigDecimal("1000"); // 회원가입 시 기본 포인트 1000
    }

    public void minusPoint(BigDecimal amount) {
        this.point = this.point.subtract(amount);
    }

    public void plusPoint(BigDecimal amount) {
        this.point = this.point.add(amount);
    }
}
