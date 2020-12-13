package com.dyz.infrastructure.dcache.config;

import com.dyz.infrastructure.dcache.DCache;
import com.dyz.infrastructure.dcache.DKeyGenerator;
import com.dyz.infrastructure.dcache.generator.OrdinaryDKeyGenerator;
import com.dyz.infrastructure.dcache.generator.SqELDKeyGenerator;
import com.dyz.infrastructure.dcache.impl.map.HashMapDCache;
import com.dyz.infrastructure.dcache.impl.redis.RedisDCache;
import com.dyz.infrastructure.dcache.serializer.JsonDCacheSerializer;
import com.dyz.infrastructure.dcache.impl.redis.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class DCacheConfig {

    @Autowired
    private RedisConfigProperties redisConfigProperties;

    @Order(95)
    @Bean
    @ConditionalOnProperty(prefix = "dcache.redis", name = "enable", havingValue = "true", matchIfMissing = false)
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMinIdle(redisConfigProperties.getMinIdle());
        jedisPoolConfig.setMaxIdle(redisConfigProperties.getMaxIdle());
        jedisPoolConfig.setMaxTotal(redisConfigProperties.getMaxTotal());
        jedisPoolConfig.setMaxWaitMillis(redisConfigProperties.getMaxWaitMillis());
        jedisPoolConfig.setTestOnBorrow(redisConfigProperties.getTestOnBorrow());
        jedisPoolConfig.setBlockWhenExhausted(true);
        return jedisPoolConfig;
    }

    @Order(96)
    @Bean
    @ConditionalOnBean(JedisPoolConfig.class)
    public JedisPool jedisPool(JedisPoolConfig jedisPoolConfig) {
        return new JedisPool(jedisPoolConfig, redisConfigProperties.getHost(), redisConfigProperties.getPort()
                , redisConfigProperties.getTimeout(), redisConfigProperties.getPassword(),
                redisConfigProperties.getDbIndex());
    }

    @Order(97)
    @Bean
    @ConditionalOnBean(JedisPool.class)
    @ConditionalOnMissingBean(RedisDCache.class)
    public DCache redisDCache(JedisPool jedisPool) {
        RedisDCache redisDCache = new RedisDCache(jedisPool);
        redisDCache.setdCacheSerializer(new JsonDCacheSerializer());
        return redisDCache;
    }

    /**
     * default DCache implement
     * @return
     */
    @Order(98)
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
