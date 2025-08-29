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
                .baselineOnMigrate(false) // Disable baseline to force migrations
                .schemas("public", "user_mgmt", "project", "notification", "collaboration", "audit", "analytics")
                .validateOnMigrate(false)
                .outOfOrder(true)
                .cleanDisabled(false) // Allow clean for troubleshooting
                .load();
        
        // Check migration info first
        System.out.println("=== FLYWAY INFO ===");
        var info = flyway.info();
        for (var migration : info.all()) {
            System.out.println("Migration: " + migration.getVersion() + " - " + migration.getDescription() + " - " + migration.getState());
        }
        
        // Check if we need to clean and recreate (for baseline ignored scenarios)
        boolean hasBaselineIgnored = false;
        boolean hasEmptySchemas = false;
        
        for (var migration : info.all()) {
            if (migration.getState().toString().contains("BASELINE_IGNORED")) {
                hasBaselineIgnored = true;
                System.out.println("Found BASELINE_IGNORED migration: " + migration.getVersion());
            }
        }
        
        // Force clean and migrate if baseline was ignored
        if (hasBaselineIgnored) {
            System.out.println("Baseline ignored detected. Cleaning database and running fresh migration...");
            try {
                flyway.clean(); // This will drop all objects in the configured schemas
                System.out.println("Database cleaned successfully");
                flyway.migrate();
                System.out.println("Fresh migration completed successfully");
            } catch (Exception e) {
                System.err.println("Error during clean and migrate: " + e.getMessage());
                // Fallback to repair and migrate
                System.out.println("Attempting repair and migrate as fallback...");
                flyway.repair();
                flyway.migrate();
            }
        } else if (info.pending().length > 0) {
            System.out.println("Running pending migrations...");
            flyway.migrate();
        } else {
            System.out.println("No pending migrations found");
        }
        
        return flyway;
    }
}