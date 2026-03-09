package com.ecommerce.user_service.service;

import com.ecommerce.snowflake.util.SnowflakeIdGenerator;
import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder argon2PasswordEncoder;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Test
    void createUser() {
        int totalCount = 100000;
        int batchSize = 1000;

        String encodedPwd = argon2PasswordEncoder.encode("123456");

        List<UserEntity> userList = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i= 0; i <= totalCount; i++) {
            String uuid = "USER-" + UUID.randomUUID().toString();
            Long snowflakeId = snowflakeIdGenerator.nextId();

            UserEntity userEntity = UserEntity.create(
                    snowflakeId,
                    "user" + i + "@test.com",
                    "player_" + i,
                    uuid,
                    encodedPwd,
                    100000
            );

            userList.add(userEntity);

            if (i % batchSize == 0) {
                userRepository.saveAll(userList);
                userList.clear();
                System.out.println(i + "명 적재중...");
            }
        }

        if (!userList.isEmpty()) {
            userRepository.saveAll(userList);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("=== 10만명 적재 완료 ===");
        System.out.println("소요 시간 : " + (endTime - startTime) / 1000.0 + "초");
    }
}