package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.UserDto;
import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.messagequeue.KafkaConsumer;
import com.ecommerce.user_service.messagequeue.KafkaConsumerConfig;
import com.ecommerce.user_service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "order-service.url=http://localhost:${wiremock.server.port}"
})
@AutoConfigureWireMock(port = 0)
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
class UserServiceImplCircuitBreakerTest {
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private KafkaConsumerConfig kafkaConsumerConfig;

    @MockitoBean
    private KafkaConsumer kafkaConsumer;

    @Test
    @DisplayName("주문 서비스 장애 시 서킷 브레이커가 작동해 빈 리스트 반환")
    void getUserByUserIdCircuitBreakerTest() {
        String testUserId = "USER-1234";
        UserEntity user = UserEntity.create(
                12345L,
                "test-cb@example.com",
                "테스트유저",
                testUserId,
                "encrypted_pwd",
                1000
        );

        userRepository.save(user);

        stubFor(get(urlMatching("/order-service/.*"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")));

        UserDto userDto = userService.getUserByUserId(testUserId);

        assertThat(userDto.getOrderList()).isEmpty();
        System.out.println("서킷 브레이커 Fallback 작동 확인 완료");
    }

    @Test
    @DisplayName("외부 서비스 응답이 10초를 초과하면 타임아웃이 발생하고 Fallback이 실행된다")
    void circuitBreaker_Timeout_Test() {
        String testUserId = "USER-1234";
        UserEntity user = UserEntity.create(
                12345L,
                "test-cb@example.com",
                "테스트유저",
                testUserId,
                "encrypted_pwd",
                1000
        );

        userRepository.save(user);

        // 응답 시간을 11초로 지연시킴 (내 설정은 10초)
        stubFor(get(urlMatching("/order-service/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(11000) // 11초 지연
                        .withHeader("Content-Type", "application/json")));

        UserDto userDto = userService.getUserByUserId(testUserId);

        assertThat(userDto.getOrderList()).isEmpty();
        System.out.println("타임아웃 기반 서킷 브레이커 작동 확인");
    }
}