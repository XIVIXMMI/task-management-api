package com.omori.taskmanagement.springboot.configuration;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

@Configuration
public class LocaleConfiguration {
   @Bean
   public org.springframework.web.servlet.LocaleResolver fixedLocaleResolver() {
        return new FixedLocaleResolver(Locale.ENGLISH);
   } 
}
