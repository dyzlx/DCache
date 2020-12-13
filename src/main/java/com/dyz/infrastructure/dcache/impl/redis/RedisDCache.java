package com.dyz.infrastructure.dcache.impl.redis;

import com.dyz.infrastructure.dcache.DCache;
import com.dyz.infrastructure.dcache.DCacheSerializer;
import com.dyz.infrastructure.dcache.serializer.ObjectDCacheSerializer;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Objects;

@Slf4j
public class RedisDCache implements DCache {

    private DCacheSerializer<Object> dCacheSerializer;

    private RedisUtils redisUtils;


    public RedisDCache(JedisPool jedisPool) {
        this.dCacheSerializer = new ObjectDCacheSerializer();
        this.redisUtils = new RedisUtils(jedisPool);
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
            jedis = redisUtils.getJedisClient();
            String jedisResult = jedis.get(key);
            if(Objects.nonNull(jedisResult)) {
                result = dCacheSerializer.deserialize(jedisResult.getBytes());
            }
            log.info("get cache from redis, key={}, result={}", key, result);
        } catch (Exception e) {
            log.error("get cache from redis error", e);
        } finally {
            redisUtils.releaseJedisClient(jedis);
        }
        return result;
    }

    @Override
    public void setCache(String key, Object value) {
        Jedis jedis = null;
        try {
            log.info("set cache to redis, key={}", key);
            jedis = redisUtils.getJedisClient();
            String result = jedis.set(key, new String(dCacheSerializer.serialize(value)));
            if(!RedisConstant.SET_RETURN_SUCCESS.equals(result)) {
                log.error("set cache to redis error, resultCode={}", result);
            }
        } catch (Exception e) {
            log.error("set cache to redis error", e);
        } finally {
            redisUtils.releaseJedisClient(jedis);
        }
    }

    @Override
    public void setCache(String key, Object value, int expired) {
        Jedis jedis = null;
        try {
            log.info("set cache to redis, key={}, expired={}s", key, expired);
            jedis = redisUtils.getJedisClient();
            String result = jedis.set(key, new String(dCacheSerializer.serialize(value)),
                    SetParams.setParams().ex(expired));
            if(!RedisConstant.SET_RETURN_SUCCESS.equals(result)) {
                log.error("set cache to redis error, resultCode={}", result);
            }
        } catch (Exception e) {
            log.error("set cache to redis error", e);
        } finally {
            redisUtils.releaseJedisClient(jedis);
        }
    }

    @Override
    public void deleteCache(String key) {
        Jedis jedis = null;
        try {
            log.info("delete cache from redis, key={}", key);
            jedis = redisUtils.getJedisClient();
            jedis.del(key);
        } catch (Exception e) {
            log.error("delete cache from redis errir", e);
        } finally {
            redisUtils.releaseJedisClient(jedis);
        }
    }

    @Override
    public boolean lockForQueryDB(String requestId) {
        return redisUtils.tryGetDistributeLock(RedisConstant.REDIS_LOCK_KEY, requestId, 1000 * 5, 500);
    }

    @Override
    public boolean unlockForQueryDB(String requestId) {
        return redisUtils.tryReleaseDistributeLock(RedisConstant.REDIS_LOCK_KEY, requestId);
    }
}
