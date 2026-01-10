package com.ecommerce.user_service.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ResponseOrder {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;
    private Date createdAt;
    private String orderId;
}
