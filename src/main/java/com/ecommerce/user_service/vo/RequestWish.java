package com.ecommerce.user_service.vo;

import lombok.Data;

import java.util.List;

@Data
public class RequestWish {
    private String productId;
    private String productName;
    private Integer price;
    private String headerImage;
    private List<String> categories;
    private List<String> genres;
}
