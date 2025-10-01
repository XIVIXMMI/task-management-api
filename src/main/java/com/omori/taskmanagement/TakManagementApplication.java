package com.omori.taskmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Locale;

@SpringBootApplication
@EnableJpaRepositories
@EnableAspectJAutoProxy
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
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
