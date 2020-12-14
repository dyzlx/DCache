package com.dyz.infrastructure.dcache.impl.redis;


import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.Objects;

@Slf4j
public class RedisUtils {

    private JedisPool jedisPool;

    public RedisUtils(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
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

    public boolean tryGetDistributeLock(Jedis jedis, String lockKey, String requestId, long expireTime) {
        //log.info("try to get redis lock, key={}, value(requestId)={}", lockKey, requestId);
        String redisReturn = jedis.set(lockKey, requestId, SetParams.setParams().nx().px(expireTime));
        if(RedisConstant.SET_RETURN_SUCCESS.equals(redisReturn)) {
            //log.info("get redis lock success");
            return true;
        }
        return false;
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
        long result;
        //log.info("try to release redis lock, key={}, value(requestId)={}", lockKey, requestId);
        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        result = (long) jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        //log.info("release redis lock result={}", result);
        return 1 == result;
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
}
