package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.dto.WishDto;
import com.ecommerce.user_service.service.WishService;
import com.ecommerce.user_service.vo.RequestWish;
import com.ecommerce.user_service.vo.ResponseWish;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wish-service")
public class WishController {
    private final WishService wishService;
    private final ModelMapper modelMapper;

    @GetMapping("/wish/list")
    public ResponseEntity<List<ResponseWish>> getWishList(@RequestHeader("userId") String userId) {
        List<WishDto> wishDtoList = wishService.getWishList(userId);

        List<ResponseWish> items = wishDtoList.stream()
                .map(dto -> modelMapper.map(dto, ResponseWish.class))
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(items);
    }

    @PostMapping("/wish/add")
    public ResponseEntity<ResponseWish> addWish(@RequestBody RequestWish request, @RequestHeader("userId") String userId) {
        WishDto dto = modelMapper.map(request, WishDto.class);
        WishDto added = wishService.addToWish(dto, userId);

        ResponseWish response = modelMapper.map(added, ResponseWish.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/wish/delete/{wishId}")
    public ResponseEntity<String> deleteWish(@PathVariable String wishId, @RequestHeader("userId") String userId) {
        wishService.removeWish(wishId, userId);

        return ResponseEntity.status(HttpStatus.OK).body("Wish has been deleted, wish ID : " + wishId);
    }
}
