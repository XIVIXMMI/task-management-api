package com.omori.taskmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Locale;

@SpringBootApplication
@EnableJpaRepositories
@EnableAspectJAutoProxy
public class TakManagementApplication {

	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);

		try {
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
		} catch (Exception e) {
			System.out.println("No .env file found, using system environment variables");
		}
		SpringApplication.run(TakManagementApplication.class, args);
	}

}
