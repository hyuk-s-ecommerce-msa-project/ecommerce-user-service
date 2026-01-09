package com.ecommerce.user_service.entity;

import com.ecommerce.user_service.dto.UserDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public static UserEntity create(String email, String name, String userId, String encryptedPwd) {
        UserEntity user = new UserEntity();

        user.email = email;
        user.name = name;
        user.userId = userId;
        user.encryptedPwd = encryptedPwd;

        return user;
    }
}
