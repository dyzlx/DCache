package com.dyz.infrastructure.dcache.impl.redis;

import com.dyz.infrastructure.dcache.DCache;
import com.dyz.infrastructure.dcache.serializer.DCacheSerializer;
import com.dyz.infrastructure.dcache.config.RedisManager;
import com.dyz.infrastructure.dcache.lock.DLock;
import com.dyz.infrastructure.dcache.lock.redis.DRedisLock;
import com.dyz.infrastructure.dcache.serializer.ObjectDCacheSerializer;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Objects;

@Slf4j
public class RedisDCache implements DCache {

    private DCacheSerializer<Object> dCacheSerializer;

    private RedisManager redisManager;

    public RedisDCache(RedisManager redisManager) {
        this.redisManager = redisManager;
        this.dCacheSerializer = new ObjectDCacheSerializer();
        log.info("redis cache init");
    }

    public void setDCacheSerializer(DCacheSerializer<Object> dCacheSerializer) {
        this.dCacheSerializer = dCacheSerializer;
    }

    @Override
    public Object getCache(String key) {
        Object result = null;
        Jedis jedis = null;
        try {
            jedis = redisManager.getJedisClient();
            String jedisResult = jedis.get(key);
            if(Objects.nonNull(jedisResult)) {
                result = dCacheSerializer.deserialize(jedisResult.getBytes());
            }
            log.info("get cache from redis, key={}, result={}", key, result);
        } catch (Exception e) {
            log.error("get cache from redis error", e);
        } finally {
            redisManager.releaseJedisClient(jedis);
        }
        return result;
    }

    @Override
    public void setCache(String key, Object value) {
        Jedis jedis = null;
        try {
            log.info("set cache to redis, key={}", key);
            jedis = redisManager.getJedisClient();
            String result = jedis.set(key, new String(dCacheSerializer.serialize(value)));
            if(!RedisConstant.SET_RETURN_SUCCESS.equals(result)) {
                log.error("set cache to redis error, resultCode={}", result);
            }
        } catch (Exception e) {
            log.error("set cache to redis error", e);
        } finally {
            redisManager.releaseJedisClient(jedis);
        }
    }

    @Override
    public void setCache(String key, Object value, int expired) {
        Jedis jedis = null;
        try {
            log.info("set cache to redis, key={}, expired={}s", key, expired);
            jedis = redisManager.getJedisClient();
            String result = jedis.set(key, new String(dCacheSerializer.serialize(value)),
                    SetParams.setParams().ex(expired));
            if(!RedisConstant.SET_RETURN_SUCCESS.equals(result)) {
                log.error("set cache to redis error, resultCode={}", result);
            }
        } catch (Exception e) {
            log.error("set cache to redis error", e);
        } finally {
            redisManager.releaseJedisClient(jedis);
        }
    }

    @Override
    public void deleteCache(String key) {
        Jedis jedis = null;
        try {
            log.info("delete cache from redis, key={}", key);
            jedis = redisManager.getJedisClient();
            jedis.del(key);
        } catch (Exception e) {
            log.error("delete cache from redis errir", e);
        } finally {
            redisManager.releaseJedisClient(jedis);
        }
    }

    @Override
    public DLock getDCacheLock() {
        return new DRedisLock(this.redisManager, "d_cache", 5 * 1000);
    }
}
