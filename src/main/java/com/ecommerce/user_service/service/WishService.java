package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.WishDto;

import java.util.List;

public interface WishService {
    WishDto addToWish(WishDto wishDto, String userId);
    void removeWish(String wishId, String userId);
    List<WishDto> getWishList(String userId);
}
