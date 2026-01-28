package com.ecommerce.user_service.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {
    private final Environment env;

    @Override
    public Exception decode(String s, Response response) {
        switch (response.status()) {
            case 400:
                if (s.contains("getOrders")) {
                        return new ResponseStatusException(HttpStatus.BAD_REQUEST, env.getProperty("order-service.exception.order_bad_request"));
                }

                if (s.contains("getCatalogs")) {
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, env.getProperty("catalog-service.exception.catalog_bad_request"));
                }

                break;
            case 404:
                if (s.contains("getOrders")) {
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, env.getProperty("order-service.exception.order_not_found"));
                }

                if (s.contains("getCatalogs")) {
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, env.getProperty("catalog-service.exception.catalog_not_found"));
                }

                break;
            default:
                if (s.contains("getOrders")) {
                    return new Exception("Order 서비스 호출 중 에러 발생");
                }
                if (s.contains("getCatalogs")) {
                    return new Exception("Catalog 서비스 호출 중 에러 발생");
                }
        }
        return new Exception("서비스 호출이 잘못되었습니다");
    }
}
