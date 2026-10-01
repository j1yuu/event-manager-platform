package kkashin.dev.eventmanager.config;

import kkashin.dev.eventmanager.utils.CacheNames;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfiguration {

    @Bean
    public RedisSerializer<Object> redisJsonSerializer() {
        return RedisSerializer.json();
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory factory,
            RedisSerializer<Object> redisJsonSerializer
    ) {
        RedisCacheConfiguration config = RedisCacheConfiguration
                .defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext
                                .SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext
                                .SerializationPair
                                .fromSerializer(redisJsonSerializer)
                )
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(5));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration(
                        CacheNames.LOCATIONS_ALL,
                        config.entryTtl(Duration.ofMinutes(2))
                )
                .withCacheConfiguration(
                        CacheNames.LOCATIONS,
                        config.entryTtl(Duration.ofMinutes(10))
                )
                .withCacheConfiguration(
                        CacheNames.EVENTS,
                        config.entryTtl(Duration.ofMinutes(5))
                )
                .build();
    }
}