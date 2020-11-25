package com.dyz.infrastructure.dcache.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class DCacheConfig {

    @Autowired
    private RedisConfigProperties redisConfigProperties;

    @Bean
    @ConditionalOnProperty(prefix = "dcache.redis", name = "enable", matchIfMissing = true)
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

    @Bean
    @ConditionalOnBean(JedisPoolConfig.class)
    public JedisPool jedisPool(JedisPoolConfig jedisPoolConfig) {
        return new JedisPool(jedisPoolConfig, redisConfigProperties.getHost(), redisConfigProperties.getPort()
                , redisConfigProperties.getTimeout(), redisConfigProperties.getPassword(), redisConfigProperties.getDbIndex());
    }
}
