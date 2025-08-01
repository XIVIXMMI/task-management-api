package com.omori.taskmanagement.springboot.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

        @Value("${spring.redis.host:localhost}")
        private String redisHost;

        @Value("${spring.redis.port:6379}")
        private int redisPort;

        @Value("${spring.redis.password:}")
        private String redisPassword;

        @Value("${spring.redis.timeout:5000}")
        private long timeout;

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
                log.info("Configuring Redis connection to {}:{}", redisHost, redisPort);
                RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
                config.setHostName(redisHost);
                config.setPort(redisPort);

                if (redisPassword != null && !redisPassword.isEmpty()) {
                        config.setPassword(redisPassword);
                }

                LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                                .commandTimeout(Duration.ofMillis(timeout))
                                .shutdownTimeout(Duration.ZERO)
                                .build();

                return new LettuceConnectionFactory(config, clientConfig);
        }

        @Bean
        public RedisCacheConfiguration cacheConfiguration() {
                ObjectMapper redisObjectMapper = JsonMapper.builder()
                                .addModule(new JavaTimeModule())
                                .build();

                // Các cấu hình quan trọng
                redisObjectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                redisObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                redisObjectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
                redisObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                // Chỉ enable type info nếu thực sự cần polymorphism
                redisObjectMapper.activateDefaultTyping(
                                redisObjectMapper.getPolymorphicTypeValidator(),
                                ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE,
                                JsonTypeInfo.As.PROPERTY);

                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(
                                redisObjectMapper);

                return RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(60))
                                .disableCachingNullValues()
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(serializer));
        }

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
                Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

                // Create ObjectMapper with type information for Redis serialization only
                ObjectMapper redisObjectMapper = JsonMapper.builder()
                                .addModule(new JavaTimeModule())
                                .build();
                redisObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                redisObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                // Enable default typing for Redis serialization only
                redisObjectMapper.activateDefaultTyping(
                                redisObjectMapper.getPolymorphicTypeValidator(),
                                ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE,
                                JsonTypeInfo.As.PROPERTY);

                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(
                                redisObjectMapper);

                // Configuration for tasks cache
                cacheConfigurations.put("tasks", RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(serializer)));

                // Configuration for taskDetails cache
                cacheConfigurations.put("taskDetails", RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(serializer)));

                return RedisCacheManager.builder(redisConnectionFactory)
                                .cacheDefaults(cacheConfiguration())
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .build();
        }
}