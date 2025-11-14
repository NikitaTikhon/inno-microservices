package com.innowise.paymentservice.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Slf4j
@Configuration
@EnableMongoAuditing
@ConditionalOnProperty(name = "spring.liquibase.enabled", havingValue = "true")
public class MongoLiquibaseConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.liquibase.change-log}")
    private String changeLog;

    @Bean
    public Liquibase liquibase() throws LiquibaseException {
        log.info("Starting Liquibase for MongoDB with URI: {}", mongoUri);
        log.info("Using changelog: {}", changeLog);

        Database database = DatabaseFactory.getInstance()
                .openDatabase(mongoUri, null, null, null, new ClassLoaderResourceAccessor());

        Liquibase liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database);

        try {
            liquibase.update("");
            log.info("Liquibase update completed successfully");
        } catch (Exception e) {
            log.error("Liquibase update failed", e);
            throw e;
        }

        return liquibase;
    }

}
