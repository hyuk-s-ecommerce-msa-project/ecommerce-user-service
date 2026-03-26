package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.security.WebSecurity;
import com.ecommerce.user_service.service.UserServiceImpl;
import com.ecommerce.user_service.vo.RequestUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WebSecurity.class
        )
)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserServiceImpl userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ModelMapper modelMapper;

    @Test
    @DisplayName("이메일 형식이 잘못되면 400 에러를 반환")
    void invalidEmailTest() throws Exception {
        // given
        RequestUser requestUser = new RequestUser("", "12345678", "testUser");
        String json = objectMapper.writeValueAsString(requestUser);

        // when & then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 400 에러를 반환")
    void invalidPasswordTest() throws Exception {
        RequestUser requestUser = new RequestUser("test@gmail.com", "123456", "testUser");
        String json = objectMapper.writeValueAsString(requestUser);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이름 형식이 잘못되면 400 에러를 반환")
    void invalidUserNameTest() throws Exception {
        RequestUser requestUser = new RequestUser("test@gmail.com", "12345678", "");
        String json = objectMapper.writeValueAsString(requestUser);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}