package com.dyz.infrastructure.dcache.lock.redis;

import com.dyz.infrastructure.dcache.connect.RedisManager;
import com.dyz.infrastructure.dcache.lock.DLock;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;


@Slf4j
public abstract class AbstractDRedisLock implements DLock {

    /**
     redis lock key prefix
     */
    private static final String REDIS_LOCK_KEY_PREFIX = "REDIS_LOCK";

    /**
     * default expire time
     */
    private static final long DEFAULT_EXPIRE_TIME = 10000;

    /**
     * identifier for different redis connection client
     */
    private String clientIdentifier;

    /**
     * key
     */
    private String lockKey;

    /**
     * redis connection
     */
    private RedisManager redisManager;

    protected long expireTime;

    AbstractDRedisLock(RedisManager redisManager, String name) {
        this(redisManager, name, DEFAULT_EXPIRE_TIME);
    }

    AbstractDRedisLock(RedisManager redisManager, String name, long expireTime) {
        this.redisManager = redisManager;
        this.expireTime = expireTime;
        this.clientIdentifier = redisManager.getClientId();
        this.lockKey = REDIS_LOCK_KEY_PREFIX + "_" + name;
        log.info("init a redis lock instance, type={}, key={}, identifier={}",
                this.getClass().getName(), this.lockKey, this.clientIdentifier);
    }

    protected abstract boolean lock(Jedis jedis, String lockKey, String lockIdentifier);

    protected abstract boolean unlock(Jedis jedis, String lockKey, String lockIdentifier);

    @Override
    public boolean lock() {
        boolean result = false;
        Jedis jedis = null;
        String lockIdentifier = getLockIdentifier();
        try {
            jedis = redisManager.getJedisClient();
            result = lock(jedis, this.lockKey, lockIdentifier);
            log.debug("acquire redis lock, key={}, lockIdentifier={}, result={}",
                    this.lockKey, lockIdentifier, result);
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
        String lockIdentifier = getLockIdentifier();
        try {
            jedis = redisManager.getJedisClient();
            result = unlock(jedis, this.lockKey, lockIdentifier);
            log.debug("release redis lock, key={}, lockIdentifier={}, result={}",
                    this.lockKey, lockIdentifier, result);
        } catch (Exception e) {
            log.error("release redis lock error", e);
        } finally {
            redisManager.releaseJedisClient(jedis);
        }
        return result;
    }

    /**
     * different client will get the different lockId,
     * to avoid release the lock which belong to other client
     * @return lockIdentifier
     */
    String getLockIdentifier() {
        return this.clientIdentifier + ":" + Thread.currentThread().getId();
    }
}