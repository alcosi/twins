package org.twins.core.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.twins.core.featurer.identityprovider.TokenMetaData;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties({
        CacheConfig.AuthTokenCacheProperties.class,
        CacheConfig.UserPermissionsCacheProperties.class
})
public class CacheConfig {

    /**
     * Configures and provides a CacheManager bean using Caffeine as the caching provider.
     * The CacheManager is set with an initial capacity of 1000 entries and an expiration
     * policy of 5 minutes after a write operation.
     * Null cache entries are allowed.
     *
     * @return a configured instance of CaffeineCacheManager with the specified settings
     */
    @Bean
    @SuppressWarnings("unchecked")
    public CacheManager cacheManager(Cache<String, TokenMetaData> authTokenCache,
                                     Cache<List<UUID>, List<UUID>> userPermissionsCache) {

        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .initialCapacity(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES);
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        caffeineCacheManager.setAllowNullValues(true);
        caffeineCacheManager.registerCustomCache("authTokenCache", (Cache<Object, Object>) (Object) authTokenCache);
        caffeineCacheManager.registerCustomCache("userPermissionsCache", (Cache<Object, Object>) (Object) userPermissionsCache);
        return caffeineCacheManager;
    }

    @Bean
    public Cache<String, TokenMetaData> authTokenCache(AuthTokenCacheProperties properties) {
        return Caffeine.newBuilder()
                .initialCapacity(properties.initialCapacity())
                .maximumSize(properties.maxSize())
                .expireAfter(Expiry.creating((String _, TokenMetaData metaData) -> {
                    Instant expiresAt = metaData.getExpiresAt();
                    if (expiresAt == null) {
                        return properties.maxTtl();
                    }
                    long secondsLeft = Duration.between(Instant.now(), expiresAt).toSeconds() - properties.clockSkew().toSeconds();
                    long finalTtlSeconds = Math.clamp(secondsLeft, 0, properties.maxTtl().toSeconds());
                    return Duration.ofSeconds(finalTtlSeconds);
                }))
                .build();
    }

    @Bean
    public Cache<List<UUID>, List<UUID>> userPermissionsCache(UserPermissionsCacheProperties properties) {
        return Caffeine.newBuilder()
                .expireAfterWrite(properties.ttl())
                .initialCapacity(properties.initialCapacity())
                .maximumSize(properties.maxSize())
                .build();
    }

    @ConfigurationProperties(prefix = "app.cache.auth-token")
    @Validated
    public record AuthTokenCacheProperties(
            @NotNull Duration maxTtl,
            @NotNull Duration clockSkew,
            @Positive int initialCapacity,
            @Positive int maxSize
    ) {}

    @ConfigurationProperties(prefix = "app.cache.user-permissions")
    @Validated
    public record UserPermissionsCacheProperties(
            @NotNull Duration ttl,
            @Positive int initialCapacity,
            @Positive int maxSize
    ) {}
}
