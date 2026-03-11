package com.ecommerce.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.kafka.KafkaSender;

@Configuration
public class ZipkinConfig {

    @Bean
    public KafkaSender kafkaSender() {
        return KafkaSender.newBuilder()
                .bootstrapServers("kafka:9093")
                .topic("zipkin")
                .build();
    }
}