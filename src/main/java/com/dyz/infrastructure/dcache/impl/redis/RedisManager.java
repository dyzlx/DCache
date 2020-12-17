package com.dyz.infrastructure.dcache.impl.redis;


import com.dyz.infrastructure.dcache.lock.RedisLock;
import com.dyz.infrastructure.dcache.lock.RedisReentrantLock;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Objects;

@Slf4j
public class RedisManager {

    private JedisPool jedisPool;

    public RedisManager(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public RedisLock newRedisLock() {
        return newRedisLock(false);
    }

    public RedisLock newRedisLock(boolean reentrant) {
        if(reentrant) {
            return new RedisReentrantLock(this);
        } else {
            return new RedisLock(this);
        }
    }

    public Jedis getJedisClient() {
        Jedis jedis = null;
        try {
            if (Objects.nonNull(jedisPool)) {
                jedis = jedisPool.getResource();
            }
        } catch (Exception e) {
            log.error("error in get jedis client", e);
        }
        if (Objects.isNull(jedis)) {
            throw new NullPointerException("Redis pool cannot be NULL.");
        }
        return jedis;
    }

    public void releaseJedisClient(Jedis client) {
        try {
            if (Objects.nonNull(client) && client.isConnected()) {
                client.close();
            }
        } catch (Exception e) {
            log.error("error in release redis connection", e);
        }
    }

    public void completelyShutDownClient(Jedis client) {
        try {
            if (Objects.nonNull(client) && client.isConnected()) {
                client.quit();
                client.disconnect();
                client.close();
            }
        } catch (Exception e) {
            log.error("error in shutdown redis connection", e);
        }
    }
}
