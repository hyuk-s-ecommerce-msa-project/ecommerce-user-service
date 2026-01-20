package com.ecommerce.user_service.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WishDto {
    private String userId;
    private String wishId;
    private String productId;
    private String productName;
    private Integer price;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private List<String> categories;
    private List<String> genres;
}
