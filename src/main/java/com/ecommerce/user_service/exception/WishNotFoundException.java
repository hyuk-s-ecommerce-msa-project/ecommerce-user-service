package com.ecommerce.user_service.exception;

public class WishNotFoundException extends RuntimeException {
    public WishNotFoundException(String message) {
        super(message);
    }
}
