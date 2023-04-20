package io.naivekyo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.naivekyo.utils.RedisUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * <p>
 *     Redis enhanced configuration.
 * </p>
 * @author NaiveKyo
 * @since 1.0
 */
@EnableCaching
@ConditionalOnClass(RedisOperations.class)
@AutoConfiguration(before = RedisAutoConfiguration.class)
public class EnhancedRedisCacheAutoConfiguration {
    
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(ObjectProvider<ObjectMapper> objectMapperProvider,
                                                       ObjectProvider<RedisConnectionFactory> connectionFactoryProvider) {
        RedisConnectionFactory redisConnectionFactory = connectionFactoryProvider.getIfAvailable();
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable();
        if (redisConnectionFactory == null)
            throw new BeanCreationException("can't creating RedisTemplate Object since no meeting RedisConnectionFactory bean within spring context!");
        if (objectMapper == null)
            throw new BeanCreationException("can't creating RedisTemplate Object since no meeting ObjectMapper Bean within spring context!");

        GenericJackson2JsonRedisSerializer redisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(redisSerializer);
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(redisSerializer);

        return redisTemplate;
    }
    
    @Bean
    @Lazy
    public RedisUtils redisUtils(ObjectProvider<RedisTemplate<Object, Object>> redisTemplateProvider) {
        return new RedisUtils(redisTemplateProvider.getIfAvailable());
    }

    /**
     * customize {@link RedisCacheManager.RedisCacheManagerBuilder}
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            ObjectProvider<CacheProperties> cachePropertiesProvider,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        CacheProperties cacheProperties = cachePropertiesProvider.getIfAvailable();
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable();
        if (cacheProperties == null)
            throw new BeanCreationException("can't get CacheProperties to creating RedisCacheManagerBuilderCustomizer bean!");
        if (objectMapper == null)
            throw new BeanCreationException("can't creating RedisCacheManagerBuilderCustomizer bean since no meeting ObjectMapper Bean within spring context!");

        return builder -> {
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
            config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));
            CacheProperties.Redis redisProperties = cacheProperties.getRedis();
            if (redisProperties.getTimeToLive() != null) {
                config = config.entryTtl(redisProperties.getTimeToLive());
            }
            if (redisProperties.getKeyPrefix() != null) {
                config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
            }
            if (!redisProperties.isCacheNullValues()) {
                config = config.disableCachingNullValues();
            }
            if (!redisProperties.isUseKeyPrefix()) {
                config = config.disableKeyPrefix();
            }
            builder.cacheDefaults(config);
        };
    }
}
