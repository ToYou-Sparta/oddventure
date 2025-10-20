package org.example.oddventure.domain.user.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oddventure.common.entity.BaseEntity;
import org.example.oddventure.domain.user.enums.UserRole;

@Entity
@Getter
@Table(name="users")
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

    @Builder
    public User(String username, String email, String password, UserRole userRole) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.point = new BigDecimal("1000"); // 회원가입 시 기본 포인트 1000
    }

    public void adjustPoint(BigDecimal amount) {
        this.point = this.point.add(amount);
    }
}
