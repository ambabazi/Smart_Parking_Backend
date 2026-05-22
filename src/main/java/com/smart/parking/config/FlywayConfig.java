package com.smart.parking.config;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    @Bean
    public FlywayConfigurationCustomizer flywayConfigurationCustomizer() {
        return (FluentConfiguration configuration) -> configuration
                .validateOnMigrate(false)
                .outOfOrder(true)
                .baselineOnMigrate(true);
    }

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            log.info("Flyway: repairing schema history (checksums + failed migrations)...");
            flyway.repair();
            var result = flyway.migrate();
            log.info("Flyway: applied {} migration(s), current version={}",
                    result.migrationsExecuted,
                    result.targetSchemaVersion);
        };
    }
}
