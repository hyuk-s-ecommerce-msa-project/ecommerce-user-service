package com.ecommerce.user_service.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUser {
    private String email;
    private String name;
    private String userId;
    private LocalDateTime createdAt;
    private Long point;
    private List<ResponseOrder> orderList;
}
