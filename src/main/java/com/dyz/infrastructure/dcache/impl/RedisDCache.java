package com.dyz.infrastructure.dcache.impl;

import com.dyz.infrastructure.dcache.DCache;
import com.dyz.infrastructure.dcache.DCacheSerializer;
import com.dyz.infrastructure.dcache.serializer.JsonDCacheSerializer;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
import java.util.Collections;
import java.util.Objects;

@Slf4j
public class RedisDCache implements DCache {

    private JedisPool jedisPool;

    private DCacheSerializer<Object> dCacheSerializer;

    private static final String SET_RETURN_SUCCESS = "OK";

    public RedisDCache(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.dCacheSerializer = new JsonDCacheSerializer();
        log.info("redis cache init");
    }

    public void setdCacheSerializer(DCacheSerializer<Object> dCacheSerializer) {
        this.dCacheSerializer = dCacheSerializer;
    }

    @Override
    public Object getCache(String key) {
        Object result = null;
        Jedis jedis = null;
        try {
            log.info("get cache from redis, key={}", key);
            jedis = getJedisClient();
            String jedisResult = jedis.get(key);
            if(Objects.nonNull(jedisResult)) {
                result = dCacheSerializer.deserialize(jedisResult.getBytes());
            }
        } catch (Exception e) {
            log.error("get cache from redis error", e);
        } finally {
            releaseJedisClient(jedis);
        }
        return result;
    }

    @Override
    public void setCache(String key, Object value) {
        Jedis jedis = null;
        try {
            log.info("set cache to redis, key={}", key);
            jedis = getJedisClient();
            String result = jedis.set(key, new String(dCacheSerializer.serialize(value)));
            if(!SET_RETURN_SUCCESS.equals(result)) {
                log.error("set cache to redis error, resultCode={}", result);
            }
        } catch (Exception e) {
            log.error("set cache to redis error", e);
        } finally {
            releaseJedisClient(jedis);
        }
    }

    @Override
    public void setCache(String key, Object value, int expired) {
        Jedis jedis = null;
        try {
            log.info("set cache to redis, key={}, expired={}s", key, expired);
            jedis = getJedisClient();
            String result = jedis.set(key, new String(dCacheSerializer.serialize(value)),
                    SetParams.setParams().ex(expired));
            if(!SET_RETURN_SUCCESS.equals(result)) {
                log.error("set cache to redis error, resultCode={}", result);
            }
        } catch (Exception e) {
            log.error("set cache to redis error", e);
        } finally {
            releaseJedisClient(jedis);
        }
    }

    @Override
    public void deleteCache(String key) {
        Jedis jedis = null;
        try {
            log.info("delete cache from redis, key={}", key);
            jedis = getJedisClient();
            jedis.del(key);
        } catch (Exception e) {
            log.error("delete cache from redis errir", e);
        } finally {
            releaseJedisClient(jedis);
        }
    }

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
