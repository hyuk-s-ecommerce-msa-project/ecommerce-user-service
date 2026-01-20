package com.ecommerce.user_service.repository;

import com.ecommerce.user_service.entity.WishEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WishRepository extends CrudRepository<WishEntity, Long> {
    WishEntity findByUserIdAndProductId(String userId, String productId);
    List<WishEntity> findByUserId(String userId);
    WishEntity findByWishId(String wishId);
}
