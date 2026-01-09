package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);
}
