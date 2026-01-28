package com.ecommerce.user_service.client;

import com.ecommerce.user_service.exception.FeignErrorDecoder;
import com.ecommerce.user_service.vo.ResponseWish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", configuration = FeignErrorDecoder.class)
public interface CatalogServiceClient {
    @GetMapping("catalog-service/catalogs/{productId}")
    ResponseWish getCatalogs(@PathVariable("productId") String productId);
}
