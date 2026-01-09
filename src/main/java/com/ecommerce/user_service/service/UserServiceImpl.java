package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.UserDto;
import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    Environment env;

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDto createUser(UserDto userDto) {
        String uuid = UUID.randomUUID().toString();

        String encryptedPwd = passwordEncoder.encode(userDto.getPwd());

        UserEntity userEntity = UserEntity.create(
                userDto.getEmail(),
                userDto.getName(),
                uuid,
                encryptedPwd
        );

        userRepository.save(userEntity);

        return modelMapper.map(userEntity, UserDto.class);
    }
}
