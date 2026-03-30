package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.TokenResponse;
import com.ecommerce.user_service.messagequeue.KafkaConsumer;
import com.ecommerce.user_service.messagequeue.KafkaConsumerConfig;
import com.ecommerce.user_service.repository.UserRepository;
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
class UserServiceImplRefreshTokenTest {
    @Autowired
    private UserServiceImpl userService;

    @MockitoBean
    private KafkaConsumerConfig kafkaConsumerConfig;

    @MockitoBean
    private KafkaConsumer kafkaConsumer;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
    @DisplayName("유효한 Refresh Token으로 요청 시 새로운 토큰 세트가 발급되고 Redis가 갱신된다")
    void reissueAccessToken_Success() {
        TokenResponse response = userService.reissueAccessToken(validRefreshToken);

        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotEqualTo(validRefreshToken);

        String savedToken = (String) redisTemplate.opsForValue().get("refreshToken:" + testUserId);
        assertThat(savedToken).isEqualTo(response.getRefreshToken());
        assertThat(savedToken).isNotEqualTo(validRefreshToken);
    }

    @Test
    @DisplayName("Redis에 저장된 토큰과 일치하지 않는 경우 예외가 발생한다")
    void reissueAccessToken_Fail_TokenMismatch() {
        String hackerToken = Jwts.builder()
                .subject(testUserId)
                .expiration(Date.from(Instant.now().plusMillis(3600000)))
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.reissueAccessToken(hackerToken);
        });

        assertThat(exception.getMessage()).contains("Refresh token is invalid or already used");
    }

    @Test
    @DisplayName("변조된 토큰으로 요청 시 파싱 에러가 발생한다")
    void reissueAccessToken_Fail_InvalidSignature() {
        String invalidToken = validRefreshToken + "tampered";

        assertThrows(RuntimeException.class, () -> {
            userService.reissueAccessToken(invalidToken);
        });
    }
}