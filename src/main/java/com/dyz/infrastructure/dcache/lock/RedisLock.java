package com.dyz.infrastructure.dcache.lock;

import com.dyz.infrastructure.dcache.impl.redis.RedisConstant;
import com.dyz.infrastructure.dcache.impl.redis.RedisManager;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * 1) lock expire
 * 2) lock timeout
 * 3) atomic
 * 4) unlock mistake
 * 5) reentrant
 *
 */
@Slf4j
public class RedisLock implements DLock {

    /**
     redis lock key
     */
    private static final String REDIS_LOCK_KEY_PREFIX = "REDIS_LOCK";

    /**
     * default expire time
     */
    private static final long DEFAULT_EXPIRE_TIME = 5 * 1000;

    /**
     * name for different redis lock
     */
    private String name;

    /**
     * identifier for different RedisLock Instance
     */
    private String redisLockIdentifier;

    /**
     * key
     */
    private String lockKey;

    /**
     * status for lock
     */
    private AtomicBoolean isLocked = new AtomicBoolean(false);

    private RedisManager redisManager;

    public RedisLock(RedisManager redisManager, String name) {
        this.name = name;
        this.redisManager = redisManager;
        this.redisLockIdentifier = UUID.randomUUID().toString();
        this.lockKey = REDIS_LOCK_KEY_PREFIX + ":" + name;
        log.info("init a redis lock instance, identifier={}", this.redisLockIdentifier);
    }

    public RedisLock setKeySuffix(String suffix) {
        this.lockKey = REDIS_LOCK_KEY_PREFIX + ":" + suffix;
        return this;
    }

    @Override
    public boolean lock() {
        boolean result = false;
        Jedis jedis = null;
        String requestId = getRequestId();
        try {
            jedis = redisManager.getJedisClient();
            result = lock(jedis, this.lockKey, requestId);
            log.debug("acquire redis lock, key={}, requestId={}, result={}",
                    this.lockKey, requestId, result);
        } catch (Exception e) {
            log.error("try acquire redis lock error", e);
        } finally {
            redisManager.releaseJedisClient(jedis);
        }
        return result;
    }

    @Override
    public boolean unlock() {
        boolean result = false;
        Jedis jedis = null;
        String requestId = getRequestId();
        try {
            jedis = redisManager.getJedisClient();
            result = unlock(jedis, this.lockKey, requestId);
            log.debug("release redis lock, key={}, requestId={}, result={}",
                    this.lockKey, requestId, result);
        } catch (Exception e) {
            log.error("release redis lock error", e);
        } finally {
            redisManager.releaseJedisClient(jedis);
        }
        return result;
    }

    protected boolean lock(Jedis jedis, String lockKey, String requestId) {
        boolean result;
        String redisReturn = jedis.set(lockKey, requestId,
                SetParams.setParams().nx().px(DEFAULT_EXPIRE_TIME));
        result = RedisConstant.SET_RETURN_SUCCESS.equals(redisReturn);
        if(result) {
            isLocked.set(true);
        }
        return result;
    }

    protected boolean unlock(Jedis jedis, String lockKey, String requestId) {
        boolean result;
        long redisReturn = (long) jedis.eval(
                "if redis.call('get',KEYS[1])==ARGV[1]" +
                        " then return redis.call('del',KEYS[1])" +
                        " else return 0 end",
                Collections.singletonList(lockKey),
                Collections.singletonList(requestId));
        log.debug("release lock lua script execute result = {}", redisReturn);
        result = (1 == redisReturn);
        if (result) {
            isLocked.set(false);
        }
        return result;
    }

    /**
     * same RedisLock instance and same thread will get the same requestId
     * @return requestId
     */
    protected String getRequestId() {
        return this.redisLockIdentifier + "_" + Thread.currentThread().getId();
    }
}