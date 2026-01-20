package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.WishDto;

import java.util.List;

public interface WishService {
    WishDto addToWish(WishDto wishDto);
    void removeWish(String wishId);
    List<WishDto> getWishList(String userId);
}
