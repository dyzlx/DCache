package com.dyz.infrastructure.dcache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
import java.util.Collections;
import java.util.Objects;

@Slf4j
@Component
@ConditionalOnBean(JedisPool.class)
public class RedisHandler {

    @Autowired
    private JedisPool jedisPool;

    private static final String SET_RETURN_SUCCESS = "OK";

    public boolean tryGetDistributeLock(Jedis jedis, String lockKey, String requestId, long expireTime) {
        String result = "";
        try {
            result = jedis.set(lockKey, requestId, SetParams.setParams().nx().px(expireTime));
        } catch (Exception e) {
            log.error("try get distribute lock error", e);
        }
        return SET_RETURN_SUCCESS.equals(result);
    }

    public boolean tryGetDistributeLock(String lockKey, String requestId, long expireTime) {
        boolean result = false;
        Jedis jedis = null;
        try {
            jedis = getJedisClient();
            result = tryGetDistributeLock(jedis, lockKey, requestId, expireTime);
        } catch (Exception e) {
            log.error("try get distribute lock error", e);
        } finally {
            releaseJedisClient(jedis);
        }
        return result;
    }

    public boolean tryReleaseDistributeLock(Jedis jedis, String lockKey, String requestId) {
        Object result = "";
        try {
            String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        } catch (Exception e) {
            log.error("try release distribute lock error", e);
        }
        return SET_RETURN_SUCCESS.equals(result);
    }

    public boolean tryReleaseDistributeLock(String lockKey, String requestId) {
        boolean result = false;
        Jedis jedis = null;
        try {
            jedis = getJedisClient();
            result = tryReleaseDistributeLock(jedis, lockKey, requestId);
        } catch (Exception e) {
            log.error("try release distribute lock error", e);
        } finally {
            releaseJedisClient(jedis);
        }
        return result;
    }

    private Jedis getJedisClient() {
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

    private void releaseJedisClient(Jedis client) {
        try {
            if (Objects.nonNull(client) && client.isConnected()) {
                client.close();
            }
        } catch (Exception e) {
            log.error("error in release redis connection", e);
        }
    }

    private void completelyShutDownClient(Jedis client) {
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
