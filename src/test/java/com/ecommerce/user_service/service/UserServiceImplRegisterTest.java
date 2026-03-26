package com.ecommerce.user_service.service;

import com.ecommerce.snowflake.util.SnowflakeIdGenerator;
import com.ecommerce.user_service.dto.UserDto;
import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplRegisterTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Mock
    private PasswordEncoder argon2PasswordEncoder;

    @Mock
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Mock
    private ModelMapper modelMapper;

    @Test
    @DisplayName("단순 회원가입")
    void createUser() {
        // given 상황 설정
        UserDto userDto = new UserDto();
        userDto.setEmail("testuser@gmail.com");
        userDto.setName("test_user");
        userDto.setPwd("12345678");
        userDto.setUserId("USER-123456");
        userDto.setPoint(1000L);
        userDto.setOrderList(List.of());

        when(snowflakeIdGenerator.nextId()).thenReturn(12345678L);
        when(argon2PasswordEncoder.encode("12345678")).thenReturn("encrypted_password");

        // modelMapper가 Entity를 다시 Dto로 바꿀 때의 가짜 결과 설정
        UserDto resultDto = new UserDto();
        resultDto.setEmail("testuser@gmail.com");
        resultDto.setUserId("USER-123456");

        when(modelMapper.map(any(), eq(UserDto.class))).thenReturn(resultDto);

        // When (실행)
        UserDto savedUser = userServiceImpl.createUser(userDto);

        // then 검증
        verify(userRepository, times(1)).save(any(UserEntity.class));

        // 결과값이 맞나?
        assertThat(savedUser.getEmail()).isEqualTo(resultDto.getEmail());
        assertThat(savedUser.getUserId()).isEqualTo(resultDto.getUserId());
        assertNotNull(resultDto);

        System.out.println("생성된 유저 : " + savedUser.getUserId());
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입 시 400 에러 반환")
    void duplicateUser() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test2@gmail.com");

        when(userRepository.findByEmail(userDto.getEmail()))
                .thenReturn(UserEntity.create(1L, "test2@gmail.com", "testUser", "uid", "12345678", 1));

        assertThrows(IllegalArgumentException.class, () -> userServiceImpl.createUser(userDto));
    }

    @Test
    @DisplayName("비밀번호 저장시 암호화되어 있어야함")
    void encryptedPassword() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@gmail.com");
        userDto.setName("test_user");
        userDto.setPwd("12345678");

        when(argon2PasswordEncoder.encode("12345678")).thenReturn("encrypted_password");
        when(snowflakeIdGenerator.nextId()).thenReturn(12345678L);

        userServiceImpl.createUser(userDto);

        verify(userRepository).save(argThat(user ->
            user.getEncryptedPwd().equals("encrypted_password") &&
                    user.getEmail().equals("test@gmail.com")
        ));
    }
}