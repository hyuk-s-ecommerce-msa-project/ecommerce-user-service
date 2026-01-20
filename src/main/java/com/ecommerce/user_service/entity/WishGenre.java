package com.ecommerce.user_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wish_genre")
public class WishGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String genreName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wish_id")
    private WishEntity wish;

    public static WishGenre create(String genreName, WishEntity wish) {
        WishGenre wishGenre = new WishGenre();

        wishGenre.genreName = genreName;
        wishGenre.wish = wish;

        return wishGenre;
    }
}
