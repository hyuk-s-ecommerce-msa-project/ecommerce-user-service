package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.dto.UserDto;
import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.service.UserService;
import com.ecommerce.user_service.vo.RequestPoint;
import com.ecommerce.user_service.vo.RequestUser;
import com.ecommerce.user_service.vo.ResponseUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
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
    public ResponseEntity<ResponseUser> createUser(@Valid @RequestBody RequestUser user) {
        UserDto userDto = modelMapper.map(user, UserDto.class);
        UserDto createUser = userService.createUser(userDto);
        ResponseUser responseUser = modelMapper.map(createUser, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @GetMapping("/info/list")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        Iterable<UserEntity> userList = userService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();

        userList.forEach(user -> result.add(modelMapper.map(user, ResponseUser.class)));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/info")
    public ResponseEntity<ResponseUser> getUser(@RequestHeader("userId") String userId) {
        UserDto userDto = userService.getUserByUserId(userId);
        ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.OK).body(responseUser);
    }

    @PostMapping("/point/withdraw")
    public ResponseEntity<ResponseUser> usePoint(@RequestHeader("userId") String userId, @RequestBody RequestPoint requestPoint) {
        Integer point = requestPoint.getPoint();
        UserDto userDto = userService.usePoint(userId, point);

        ResponseUser response = modelMapper.map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/point/increase")
    public ResponseEntity<ResponseUser> addPoint(@RequestHeader("userId") String userId, @RequestBody RequestPoint requestPoint) {
        Integer point = requestPoint.getPoint();
        UserDto userDto = userService.addPoint(userId, point);
        ResponseUser response = modelMapper.map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
