package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.UserDto;
import com.ecommerce.user_service.entity.UserEntity;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUserByUserId(String userId);
    Iterable<UserEntity> getUserByAll();
    UserDto usePoint(String userId, Integer usedPoint);
    UserDto addPoint(String userId, Integer usedPoint);
}
