package com.innowise.paymentservice.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;


@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EnableWireMock({
        @ConfigureWireMock(name = "random-number-api", baseUrlProperties = "services.random-number-api.uri")
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTest {

    @Container
    static MongoDBContainer mongoDB = new MongoDBContainer("mongo:8.2.1");

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:4.0.1");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDB::getConnectionString);
        registry.add("spring.data.mongodb.database", () -> "payment");
        
        registry.add("spring.liquibase.enabled", () -> "false");
        
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("services.random-number-api.fallback-value", () -> 2);
    }

    protected static String getKafkaBootstrapServers() {
        return kafka.getBootstrapServers();
    }

}

