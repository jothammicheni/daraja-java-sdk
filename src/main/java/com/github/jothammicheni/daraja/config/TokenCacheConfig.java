package com.github.jothammicheni.daraja_springboot_starter_jdk.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class TokenCacheConfig {

    /**
     * Distributed Redis Cache Manager configuration.
     * This bean is only built if Redis dependency classes are on the user's classpath
     * AND they explicitly configure mpesa.daraja.cache-type=redis.
     */
    @Bean
    @Primary
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnProperty(name = "mpesa.daraja.cache-type", havingValue = "redis")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("mpesa-token", defaultCacheConfig.entryTtl(Duration.ofMinutes(50)));
        cacheConfigurations.put("mpesa-idempotency", defaultCacheConfig.entryTtl(Duration.ofHours(24)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Local In-Memory Fallback Cache Manager.
     * This activates if mpesa.daraja.cache-type is explicitly missing, set to "local",
     * or if the application is running without Redis dependencies in its classpath.
     */
    @Bean
    @ConditionalOnProperty(name = "mpesa.daraja.cache-type", havingValue = "local", matchIfMissing = true)
    public CacheManager localCacheManager() {
        return new ConcurrentMapCacheManager("mpesa-token", "mpesa-idempotency");
    }
}
