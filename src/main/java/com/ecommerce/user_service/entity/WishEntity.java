package com.ecommerce.user_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wish")
@EntityListeners(AuditingEntityListener.class)
public class WishEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String thumbnailUrl;

    @OneToMany(mappedBy = "wish", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "wish", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishGenre> genres = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    public static WishEntity create(String wishId, String userId, String productId, String productName, Integer price, String thumbnailUrl) {
        WishEntity wish = new WishEntity();

        wish.wishId = wishId;
        wish.userId = userId;
        wish.productId = productId;
        wish.productName = productName;
        wish.price = price;
        wish.thumbnailUrl = thumbnailUrl;
        wish.createdAt = LocalDateTime.now();

        return wish;
    }
}
