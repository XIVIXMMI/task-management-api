package com.omori.taskmanagement.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
public class FlywayConfiguration {

    @Bean
    @DependsOn("flyway")
    public FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway, null);
    }

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .schemas("public", "user_mgmt", "project", "notification", "collaboration", "audit", "analytics")
                .validateOnMigrate(false)
                .outOfOrder(true)
                .load();
        
        // Check migration info first
        System.out.println("=== FLYWAY INFO ===");
        var info = flyway.info();
        for (var migration : info.all()) {
            System.out.println("Migration: " + migration.getVersion() + " - " + migration.getDescription() + " - " + migration.getState());
        }
        
        // Only migrate if needed
        if (info.pending().length > 0) {
            System.out.println("Running pending migrations...");
            flyway.migrate();
        } else {
            System.out.println("No pending migrations found");
        }
        
        return flyway;
    }
}