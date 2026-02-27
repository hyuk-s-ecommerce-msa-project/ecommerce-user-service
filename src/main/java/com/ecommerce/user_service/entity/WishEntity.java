package com.ecommerce.user_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wish")
@EntityListeners(AuditingEntityListener.class)
public class WishEntity {
    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    private String wishId;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private String productId;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false)
    private Integer price;
    @Column(nullable = false)
    private String headerImage;

    @OneToMany(mappedBy = "wish", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WishCategory> categories = new LinkedHashSet<>();

    @OneToMany(mappedBy = "wish", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WishGenre> genres = new LinkedHashSet<>();

    @CreatedDate
    private LocalDateTime createdAt;

    public static WishEntity create(Long id, String wishId, String userId, String productId, String productName, Integer price, String headerImage) {
        WishEntity wish = new WishEntity();

        wish.id = id;
        wish.wishId = wishId;
        wish.userId = userId;
        wish.productId = productId;
        wish.productName = productName;
        wish.price = price;
        wish.headerImage = headerImage;
        wish.createdAt = LocalDateTime.now();

        return wish;
    }
}
