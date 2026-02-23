package com.ecommerce.user_service.service;

import com.ecommerce.user_service.client.OrderServiceClient;
import com.ecommerce.user_service.dto.TokenResponse;
import com.ecommerce.user_service.dto.UserDto;
import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.repository.UserRepository;
import com.ecommerce.user_service.vo.ResponseOrder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final Environment env;

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    private final OrderServiceClient orderServiceClient;

    private final CircuitBreakerFactory circuitBreakerFactory;

    @Override
    public void storeRefreshToken(String userId, String refreshToken, long expirationTime) {
        redisTemplate.opsForValue().set(
                "refreshToken:" + userId,
                refreshToken,
                Duration.ofMillis(expirationTime)
        );
    }

    @Override
    @Transactional
    public TokenResponse reissueAccessToken(String refreshToken) {
        byte[] secretKeyBytes = env.getProperty("token.secret").getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);

        String userId;

        try {
            userId = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token");
        }

        String savedToken = (String) redisTemplate.opsForValue().get("refreshToken:" + userId);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new RuntimeException("Refresh token expired or invalid");
        }

        Instant now = Instant.now();

        String newAccessToken = Jwts.builder()
                .subject(userId)
                .expiration(Date.from(now.plusMillis(Long.parseLong(env.getProperty("token.access-token.expiration-time")))))
                .issuedAt(Date.from(now))
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();

        long refreshTime = Long.parseLong(env.getProperty("token.refresh-token.expiration-time"));

        String newRefreshToken = Jwts.builder()
                .subject(userId)
                .expiration(Date.from(now.plusMillis(refreshTime)))
                .issuedAt(Date.from(now))
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();

        storeRefreshToken(userId, newRefreshToken, refreshTime);

        return TokenResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException(username + ": Not Found");
        }

        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
                true, true, true, true, new ArrayList<>());
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        return modelMapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        String uuid = "USER-" + UUID.randomUUID().toString();

        String encryptedPwd = passwordEncoder.encode(userDto.getPwd());

        UserEntity userEntity = UserEntity.create(
                userDto.getEmail(),
                userDto.getName(),
                uuid,
                encryptedPwd,
                1000
        );

        userRepository.save(userEntity);

        return modelMapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = modelMapper.map(userEntity, UserDto.class);

//        List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitBreaker");
        List<ResponseOrder> orderList = circuitBreaker.run(() -> orderServiceClient.getOrders(userId),
                throwable -> new ArrayList<>());

        userDto.addOrder(orderList);

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public UserDto usePoint(String userId, Integer usedPoint) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        userEntity.decreasePoint(usedPoint);

        return modelMapper.map(userEntity, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto addPoint(String userId, Integer usedPoint) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        userEntity.increasePoint(usedPoint);

        return modelMapper.map(userEntity, UserDto.class);
    }
}
