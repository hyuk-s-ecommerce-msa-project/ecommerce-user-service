package com.ecommerce.user_service.entity;

import com.ecommerce.user_service.dto.UserDto;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Long point;
    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    public static UserEntity create(String email, String name, String userId, String encryptedPwd,  Long point) {
        UserEntity user = new UserEntity();

        user.email = email;
        user.name = name;
        user.userId = userId;
        user.encryptedPwd = encryptedPwd;
        user.point = point;

        return user;
    }
}
