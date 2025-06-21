package com.omori.taskmanagement.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableJpaRepositories
@EnableAspectJAutoProxy
public class TakManagementApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.load(); // ✅ loads .env automatically
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
		SpringApplication.run(TakManagementApplication.class, args);
	}

}
