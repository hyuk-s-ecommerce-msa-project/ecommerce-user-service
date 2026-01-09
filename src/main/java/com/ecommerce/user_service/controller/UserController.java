package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.dto.UserDto;
import com.ecommerce.user_service.service.UserService;
import com.ecommerce.user_service.vo.RequestUser;
import com.ecommerce.user_service.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-service")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final Environment env;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping("/health-check")
    public String status() {
        return String.format("It's working in User Service" +
                ", port(local.server.port)=" + env.getProperty("local.server.port") +
                ", port(server.port)=" + env.getProperty("server.port"));
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {
        UserDto userDto = modelMapper.map(user, UserDto.class);
        UserDto createUser = userService.createUser(userDto);
        ResponseUser responseUser = modelMapper.map(createUser, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }
}
