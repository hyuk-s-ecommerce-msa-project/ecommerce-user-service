package com.ecommerce.user_service.service;

import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.messagequeue.KafkaConsumer;
import com.ecommerce.user_service.messagequeue.KafkaConsumerConfig;
import com.ecommerce.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.kafka.enabled=false",
        "token.secret=test_secret_key_for_unit_testing",
        "gateway.ip=127.0.0.1",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
@Testcontainers
class UserServiceImplPointConcurrencyTest {
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private KafkaConsumerConfig kafkaConsumerConfig;

    @MockitoBean
    private KafkaConsumer kafkaConsumer;

    static {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            System.setProperty("DOCKER_HOST", "npipe:////./pipe/docker_engine");
            System.setProperty("TESTCONTAINERS_RYUK_DISABLED", "true");
        }
    }

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:10.11")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        UserEntity user = UserEntity.create(123456L, "test@gmail.com", "통합테스트",
                "USER-1234", "hash_pwd", 10000);
        userRepository.save(user);
    }

    @Test
    @DisplayName("통합 테스트 : 비관적 락을 통해 100명 동시 차감 시 잔액 0원 검증")
    void concurrencyUsePoint() throws InterruptedException {
        final String userId = "USER-1234";

        UserEntity user = UserEntity.create(123456L, "test@gmail.com", "통합테스트",
                userId, "hash_pwd", 10000);

        userRepository.save(user);

        int threadCount = 100;
        int useAmount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 실제 DB에 락을 걸고 데이터를 수정함
                    userService.usePoint(userId, useAmount);
                } catch (Exception e) {
                    System.err.println("에러 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        UserEntity resultUser = userRepository.findForTestByUserId(userId);
        System.out.println("최종 포인트: " + resultUser.getPoint());

        assertThat(resultUser.getPoint()).isEqualTo(0);
    }

    @Test
    @DisplayName("통합 테스트 : 100명 동시 충전시 포인트 10,000 검증")
    void addPoint() throws InterruptedException {
        final String userId = "USER-1234";

        UserEntity user = UserEntity.create(123456L, "test@gmail.com", "통합테스트",
                userId, "hash_pwd", 0);

        userRepository.save(user);

        int threadCount = 100;
        int useAmount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 실제 DB에 락을 걸고 데이터를 수정함
                    userService.addPoint(userId, useAmount);
                } catch (Exception e) {
                    System.err.println("에러 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        UserEntity resultUser = userRepository.findForTestByUserId(userId);
        System.out.println("최종 포인트: " + resultUser.getPoint());

        assertThat(resultUser.getPoint()).isEqualTo(10000);
    }
}