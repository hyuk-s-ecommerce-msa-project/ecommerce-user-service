package com.ecommerce.user_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wish_category")
public class WishCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String categoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wish_id")
    private WishEntity wish;

    public static WishCategory create(String categoryName, WishEntity wish) {
        WishCategory wishCategory = new WishCategory();

        wishCategory.categoryName = categoryName;
        wishCategory.wish = wish;

        return wishCategory;
    }
}
