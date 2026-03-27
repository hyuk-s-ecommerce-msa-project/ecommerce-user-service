package com.ecommerce.user_service.security;

import com.ecommerce.user_service.dto.UserDto;
import com.ecommerce.user_service.service.UserServiceImpl;
import com.ecommerce.user_service.vo.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoginTest {
    @Mock
    private UserServiceImpl userService;

    @Mock
    private Environment env;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthenticationFilter authenticationFilter;

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        authenticationFilter = new AuthenticationFilter(userService, env, objectMapper, authenticationManager);
    }

    @Test
    @DisplayName("성공 : JSON 요청이 객체로 변환되고 인증 매니저가 호출되는가")
    void attemptAuthenticationSuccess() throws Exception {
        RequestLogin loginRequest = new RequestLogin();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("12345678");

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(loginJson.getBytes());

        authenticationFilter.attemptAuthentication(request, new MockHttpServletResponse());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("성공 : 로그인 성공시 JWT 토큰이 생성되고 헤더에 포함되는가")
    void successfulAuthentication() throws Exception {
        User user = new User("test@gmail.com", "12345678", new ArrayList<>());
        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(user);

        UserDto userDto = new UserDto();
        userDto.setUserId("USER-1234");
        given(userService.getUserDetailsByEmail("test@gmail.com")).willReturn(userDto);

        given(env.getProperty("token.secret")).willReturn("very-secret-key-more-than-512-bits-for-security-reasons-1234567890");
        given(env.getProperty("token.access-token.expiration-time")).willReturn("3600000");
        given(env.getProperty("token.refresh-token.expiration-time")).willReturn("86400000");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        authenticationFilter.successfulAuthentication(request, response, mock(FilterChain.class), authentication);

        assertNotNull(response.getHeader("accessToken"));
        assertNotNull(response.getHeader("refreshToken"));
        assertEquals("USER-1234", response.getHeader("userId"));

        verify(userService).storeRefreshToken(eq("USER-1234"), anyString(), anyLong());
    }

    @Test
    @DisplayName("실패 : 인증 실패 시 401 에러 발생")
    void unsuccessfulAuthentication() throws Exception {
        AuthenticationException fail = new BadCredentialsException("Invalid Email");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        authenticationFilter.unsuccessfulAuthentication(request, response, fail);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    @DisplayName("실패 : 이메인이 빈 문자열일 때 인증 실패")
    void attemptAuthenticationInvalidEmail() throws Exception {
        RequestLogin loginRequest = new RequestLogin();
        loginRequest.setEmail("");
        loginRequest.setPassword("12345678");

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(loginJson.getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("Invalid Email"));

        assertThrows(RuntimeException.class, () -> {
            authenticationFilter.attemptAuthentication(request, response);
        });
    }

    @Test
    @DisplayName("실패 : 비밀번호가 빈 문자열일 때 인증실패")
    void attemptAuthenticationInvalidPassword() throws Exception {
        RequestLogin loginRequest = new RequestLogin();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("");

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(loginJson.getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("Invalid Password"));

        assertThrows(RuntimeException.class, () -> {
            authenticationFilter.attemptAuthentication(request, response);
        });
    }

    @Test
    @DisplayName("실패 : JSON 형식이 아닐 때 500 에러 반환")
    void attemptAuthenticationInvalidJSON() throws Exception {
        doThrow(new IOException("JSON Parsing Error"))
                .when(objectMapper).readValue(any(InputStream.class), eq(RequestLogin.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent("{}".getBytes());

        assertThrows(RuntimeException.class, () -> {
            authenticationFilter.attemptAuthentication(request, new MockHttpServletResponse());
        });
    }
}