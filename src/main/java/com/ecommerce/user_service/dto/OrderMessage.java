package com.ecommerce.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderMessage {
    private Payload payload;

    @Data
    public static class Payload {
        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("used_point")
        private Integer usedPoint;
    }
}
