package com.ecommerce.user_service.client;

import com.ecommerce.user_service.exception.FeignErrorDecoder;
import com.ecommerce.user_service.vo.ResponseOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "order-service", configuration = FeignErrorDecoder.class)
public interface OrderServiceClient {
    @GetMapping("/order-service/orders")
    List<ResponseOrder> getOrders(@RequestHeader("userId") String userId);
}
