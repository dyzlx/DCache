package com.dyz.infrastructure.dcache.impl.redis;

import com.dyz.infrastructure.dcache.DCache;
import com.dyz.infrastructure.dcache.DCacheSerializer;
import com.dyz.infrastructure.dcache.serializer.ObjectDCacheSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Objects;
import java.util.UUID;

@Slf4j
public class RedisDCache implements DCache {

    private DCacheSerializer<Object> dCacheSerializer;

    private RedisUtils redisUtils;


    public RedisDCache(JedisPool jedisPool) {
        this.dCacheSerializer = new ObjectDCacheSerializer();
        this.redisUtils = new RedisUtils(jedisPool);
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
    public Object missCacheResetWithLock(String key, ProceedingJoinPoint point, int expireTime, long timeout)
            throws Throwable {
        String lockKey = RedisConstant.REDIS_LOCK_KEY + key;
        String requestId = UUID.randomUUID().toString();
        Object result;
        if(redisUtils.tryGetDistributeLock(lockKey, requestId, timeout)) {
            try {
                result = this.getCache(key);
                if(Objects.nonNull(result)) {
                    return result;
                }
                result = point.proceed();
                this.setCache(key, result, expireTime);
            } catch (Throwable e) {
                log.error("error when query db then set redis cache with lock, key={}", key);
                throw e;
            } finally {
                redisUtils.tryReleaseDistributeLock(lockKey, requestId);
            }
        } else {
            result = missCacheResetWithLock(key, point, expireTime, timeout);
        }
        return result;
    }
}
