package com.ecommerce.user_service.dto;

import com.ecommerce.user_service.vo.ResponseOrder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDto {
    private String email;
    private String name;
    private String pwd;
    private String userId;
    private LocalDateTime createdAt;
    private String encryptedPwd;
    private Long point;
    private List<ResponseOrder> orderList;

    public void addOrder(List<ResponseOrder> orderList){
        this.orderList = orderList;
    }
}
