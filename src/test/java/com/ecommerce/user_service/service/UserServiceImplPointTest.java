package com.ecommerce.user_service.service;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplPointTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private ModelMapper modelMapper;

    @Test
    @DisplayName("성공 : 포인트 차감")
    void usePoint() {
        // given
        UserEntity user = UserEntity.create(123456L, "test@gmail.com", "test",
                "USER-1234", "encrypted_password", 4500);

        when(userRepository.findByUserId("USER-1234")).thenReturn(user);
        when(modelMapper.map(any(), eq(UserDto.class))).thenAnswer(i -> {
            UserEntity entity = i.getArgument(0);
            UserDto userDto = new UserDto();
            userDto.setPoint(Long.valueOf(entity.getPoint()));

            return userDto;
        });

        // when
        UserDto result = userService.usePoint("USER-1234", 500);

        // then
        assertThat(result.getPoint()).isEqualTo(4000);
        verify(userRepository).findByUserId("USER-1234");
    }

    @Test
    @DisplayName("성공 : 포인트 충전")
    void addPoint() {
        UserEntity user = UserEntity.create(123456L, "test@gmail.com", "test",
                "USER-1234", "encrypted_password", 4000);

        when(userRepository.findByUserId("USER-1234")).thenReturn(user);
        when(modelMapper.map(any(), eq(UserDto.class))).thenAnswer(i -> {
            UserEntity entity = i.getArgument(0);
            UserDto userDto = new UserDto();
            userDto.setPoint(Long.valueOf(entity.getPoint()));
            return userDto;
        });

        UserDto result = userService.addPoint("USER-1234", 500);

        assertThat(result.getPoint()).isEqualTo(4500);
        verify(userRepository).findByUserId("USER-1234");
    }

    @Test
    @DisplayName("실패 : 포인트 부족시 포인트 차감 실패")
    void failInsufficientPoint() {
        UserEntity user = UserEntity.create(123456L, "test@gmail.com", "test",
                "USER-1234", "encrypted_password", 100);

        when(userRepository.findByUserId("USER-1234")).thenReturn(user);

        assertThrows(RuntimeException.class, () -> userService.usePoint("USER-1234", 5000));
        assertThat(user.getPoint()).isEqualTo(100);
    }

    @Test
    @DisplayName("실패 : 존재하지 않는 유저의 포인트 조회 시 예외 발생")
    void failUserNotFound() {
        when(userRepository.findByUserId("USER-NON-Exists")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> userService.usePoint("USER-NON-Exists", 5000));
        verify(userRepository).findByUserId("USER-NON-Exists");
    }

    @Test
    @DisplayName("실패 : 포인트가 음수로 들어갈 때 예외 발생")
    void failMinusPoint() {
        String userId = "USER-1234";

        assertThrows(RuntimeException.class, () -> userService.usePoint(userId, -500));
    }

//    @Test
//    @DisplayName("동시성 테스트 : 100명이 동시에 100원씩 차감하면 잔액이 정확히 남아야함")
//    void concurrencyUserPointTest() throws InterruptedException {
//        // 10,000 포인트를 가진 유저 설정
//        final String userId = "USER-1234";
//        final int initialPoint = 10000;
//        final int useAmount = 100;
//        final int threadCount = 100;
//
//        UserEntity user = UserEntity.create(123456L, "test@gmail.com", "test",
//                "USER-1234", "encrypted_password", initialPoint);
//
//        when(userRepository.findByUserId("USER-1234")).thenReturn(user);
//
//        ExecutorService executorService = Executors.newFixedThreadPool(32);
//        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
//
//        for (int i = 0; i < threadCount; i++) {
//            executorService.submit(() -> {
//                try {
//                    userService.usePoint(userId, useAmount);
//                } finally {
//                    countDownLatch.countDown();
//                }
//            });
//        }
//
//        countDownLatch.await();
//
//        assertThat(user.getPoint()).isEqualTo(0);
//    }
}