package com.dyz.infrastructure.dcache.config;

import com.dyz.infrastructure.dcache.DCache;
import com.dyz.infrastructure.dcache.DKeyGenerator;
import com.dyz.infrastructure.dcache.generator.OrdinaryDKeyGenerator;
import com.dyz.infrastructure.dcache.generator.SqELDKeyGenerator;
import com.dyz.infrastructure.dcache.impl.map.HashMapDCache;
import com.dyz.infrastructure.dcache.impl.redis.RedisDCache;
import com.dyz.infrastructure.dcache.serializer.JsonDCacheSerializer;
import com.dyz.infrastructure.dcache.serializer.ObjectDCacheSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Optional;

@Configuration
public class DCacheConfig {

    @Autowired
    private RedisConfigProperties redisConfigProperties;

    @Bean
    @ConditionalOnProperty(prefix = "dcache.redis", name = "enable", havingValue = "true", matchIfMissing = false)
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMinIdle(
                Optional.ofNullable(redisConfigProperties.getMinIdle()).orElse(10));
        jedisPoolConfig.setMaxIdle(
                Optional.ofNullable(redisConfigProperties.getMaxIdle()).orElse(15));
        jedisPoolConfig.setMaxTotal(
                Optional.ofNullable(redisConfigProperties.getMaxTotal()).orElse(20));
        jedisPoolConfig.setMaxWaitMillis(
                Optional.ofNullable(redisConfigProperties.getMaxWaitMillis()).orElse(500));
        jedisPoolConfig.setTestOnBorrow(
                Optional.ofNullable(redisConfigProperties.getTestOnBorrow()).orElse(false));
        jedisPoolConfig.setBlockWhenExhausted(true);
        return jedisPoolConfig;
    }

    @Bean
    @ConditionalOnBean(JedisPoolConfig.class)
    public JedisPool jedisPool(JedisPoolConfig jedisPoolConfig) {
        return new JedisPool(jedisPoolConfig,
                redisConfigProperties.getHost(),
                redisConfigProperties.getPort(),
                Optional.ofNullable(redisConfigProperties.getTimeout()).orElse(500),
                redisConfigProperties.getPassword(),
                Optional.ofNullable(redisConfigProperties.getDbIndex()).orElse(0));
    }

    @Bean
    @ConditionalOnBean(JedisPool.class)
    public DCache redisDCache(JedisPool jedisPool) {
        RedisDCache redisDCache = new RedisDCache(jedisPool);
        RedisConfigProperties.Serializer serializer = redisConfigProperties.getSerializer();
        if(RedisConfigProperties.Serializer.binary.equals(serializer)) {
            redisDCache.setDCacheSerializer(new ObjectDCacheSerializer());
        } else {
            redisDCache.setDCacheSerializer(new JsonDCacheSerializer());
        }
        return redisDCache;
    }

    @Bean
    @ConditionalOnMissingBean(DCache.class)
    public DCache hashMapDCache() {
        return new HashMapDCache();
    }

    @Bean(name = "ordinaryDKeyGenerator")
    public DKeyGenerator ordinaryDKeyGenerator() {
        return new OrdinaryDKeyGenerator();
    }

    @Bean(name = "sqELDKeyGenerator")
    public DKeyGenerator sqELDKeyGenerator() {
        return new SqELDKeyGenerator();
    }
}
