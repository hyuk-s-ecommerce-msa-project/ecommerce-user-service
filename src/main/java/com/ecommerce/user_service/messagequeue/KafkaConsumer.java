package com.ecommerce.user_service.messagequeue;

import com.ecommerce.user_service.dto.OrderMessage;
import com.ecommerce.user_service.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final UserService userService;

    @KafkaListener(topics = "order-cancel-topic", groupId = "userConsumerGroup")
    @Transactional
    public void restorePoint(String kafkaMessage) {
        log.info("Cancel Event Received - Kafka Message: {}", kafkaMessage);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            OrderMessage message = objectMapper.readValue(kafkaMessage, OrderMessage.class);

            String userId = message.getPayload().getUserId();
            Integer usedPoint = message.getPayload().getUsedPoint();

            if (userId != null && usedPoint != null) {
                userService.addPoint(userId, usedPoint);
                log.info("Successfully restored point for user: {}", userId);
            }
        } catch (Exception e) {
            log.error("Error restoring point: {}", e.getMessage());
        }
    }
}
