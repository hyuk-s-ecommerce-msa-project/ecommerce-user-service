package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.WishDto;

import java.util.List;

public interface WishService {
    WishDto addToWish(String userId, String productId);
    void removeWish(String wishId, String userId);
    List<WishDto> getWishList(String userId);
}
