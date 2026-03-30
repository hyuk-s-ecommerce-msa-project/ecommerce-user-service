package com.ecommerce.user_service.service;

import com.ecommerce.user_service.messagequeue.KafkaConsumer;
import com.ecommerce.user_service.messagequeue.KafkaConsumerConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.kafka.enabled=false",
        "token.secret=test_secret_key_for_unit_testing_123456789123456789123456789123456789123456789",
        "token.access-token.expiration-time=3600000",
        "token.refresh-token.expiration-time=86400000",
        "gateway.ip=127.0.0.1",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class UserServiceImplLogoutTest {
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private KafkaConsumerConfig kafkaConsumerConfig;

    @MockitoBean
    private KafkaConsumer kafkaConsumer;

    @Autowired
    private Environment env;

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    private String testUserId;
    private String validRefreshToken;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        testUserId = "USER-" + UUID.randomUUID().toString();
        byte[] secretKeyBytes = env.getProperty("token.secret").getBytes(StandardCharsets.UTF_8);
        secretKey = Keys.hmacShaKeyFor(secretKeyBytes);

        Instant now = Instant.now();
        validRefreshToken = Jwts.builder()
                .subject(testUserId)
                .expiration(Date.from(now.plusMillis(3600000)))
                .issuedAt(Date.from(now))
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();

        redisTemplate.opsForValue().set("refreshToken:" + testUserId, validRefreshToken);
    }

    @Test
    @DisplayName("로그아웃 시 Redis에서 Refresh Token이 삭제되어야 한다")
    void logout_Success() {
        String redisKey = "refreshToken:" + testUserId;
        redisTemplate.opsForValue().set(redisKey, validRefreshToken);

        assertThat(redisTemplate.opsForValue().get(redisKey)).isEqualTo(validRefreshToken);

        userService.logout(testUserId);

        Object deletedToken = redisTemplate.opsForValue().get(redisKey);

        assertThat(deletedToken).isNull();
    }
}