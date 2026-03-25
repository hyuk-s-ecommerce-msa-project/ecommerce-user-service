package com.ecommerce.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import zipkin2.reporter.kafka.KafkaSender;

@Configuration
public class ZipkinConfig {
    private final Environment env;

    public ZipkinConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public KafkaSender kafkaSender() {
        String bootstrapServers = env.getProperty("management.zipkin.kafka.bootstrap-servers", "localhost:9093");

        return KafkaSender.newBuilder()
                .bootstrapServers(bootstrapServers)
                .topic("zipkin")
                .build();
    }
}