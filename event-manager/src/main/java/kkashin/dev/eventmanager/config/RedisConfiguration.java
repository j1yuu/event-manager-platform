package kkashin.dev.eventmanager.config;

import kkashin.dev.eventmanager.utils.CacheNames;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
public class RedisConfiguration {

    @Bean
    RedisCacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            RedisSerializer<Object> jsonSerializer
    ) {
        var defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10));

        var perCache = Map.of(
                CacheNames.LOCATIONS_ALL, defaults.entryTtl(Duration.ofMinutes(2)),
                CacheNames.LOCATIONS, defaults.entryTtl(Duration.ofMinutes(10)),
                CacheNames.EVENTS, defaults.entryTtl(Duration.ofMinutes(5))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .build();
    }
}
