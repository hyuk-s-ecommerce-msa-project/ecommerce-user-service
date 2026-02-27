package com.ecommerce.user_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    @Id
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String email;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, unique = true)
    private String userId;
    @Column(nullable = false)
    private String encryptedPwd;
    @Column(nullable = false)
    private Integer point;
    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    public static UserEntity create(Long id, String email, String name, String userId, String encryptedPwd, Integer point) {
        UserEntity user = new UserEntity();

        user.id = id;
        user.email = email;
        user.name = name;
        user.userId = userId;
        user.encryptedPwd = encryptedPwd;
        user.point = point;

        return user;
    }

    public void decreasePoint(Integer amount) {
        if (this.point < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        this.point -= amount;
    }

    public void increasePoint(Integer amount) {
        this.point += amount;
    }
}
