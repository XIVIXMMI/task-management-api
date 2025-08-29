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
        
        // Check if V1 was baseline ignored and force repair + migrate
        boolean v1Ignored = false;
        for (var migration : info.all()) {
            if ("1".equals(migration.getVersion() != null ? migration.getVersion().getVersion() : "") 
                && migration.getState().toString().contains("BASELINE_IGNORED")) {
                v1Ignored = true;
                break;
            }
        }
        
        if (v1Ignored) {
            System.out.println("V1 migration was baseline ignored. Repairing and migrating...");
            flyway.repair();
            flyway.migrate();
        } else if (info.pending().length > 0) {
            System.out.println("Running pending migrations...");
            flyway.migrate();
        } else {
            System.out.println("No pending migrations found");
        }
        
        return flyway;
    }
}