package com.ecommerce.user_service.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResponseWish {
    private String userId;
    private String productId;
    private String productName;
    private String wishId;
    private Integer price;
    private List<String> categories;
    private List<String> genres;
    private String headerImage;
    private LocalDateTime createdAt;
}
