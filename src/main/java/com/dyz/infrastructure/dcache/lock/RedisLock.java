package com.dyz.infrastructure.dcache.lock;

import com.dyz.infrastructure.dcache.impl.redis.RedisConstant;
import com.dyz.infrastructure.dcache.impl.redis.RedisManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class RedisLock implements DCacheLock {

    /*
     redis lock key
     */
    private static final String REDIS_LOCK_KEY = "D_REDIS_LOCK";

    /**
     * default expire time
     */
    private static final long DEFAULT_EXPIRE_TIME = 5 * 1000;

    /**
     * key
     */
    private String key = REDIS_LOCK_KEY;

    /**
     * each redis lock has different id, to avoid release other redis lock by mistake
     */
    private String requestId;

    /**
     * status for lock
     */
    private AtomicBoolean isLocked = new AtomicBoolean(false);

    /**
     * lua script for release redis lock
     */
    private static final String RELEASE_LOCK_LUA_SCRIPT =
            "if redis.call('get',KEYS[1])==ARGV[1]" +
                    " then return redis.call('del',KEYS[1])" +
                    " else return 0 end";

    private RedisManager redisManager;

    public RedisLock(RedisManager redisManager) {
        this.redisManager = redisManager;
    }

    public RedisLock setKeySuffix(String suffix) {
        this.key = this.key + "_" + suffix;
        return this;
    }

    @Override
    public boolean lock() {
        boolean result = false;
        Jedis jedis = null;
        this.requestId = UUID.randomUUID().toString();
        try {
            jedis = redisManager.getJedisClient();
            result = lock(jedis, this.key, this.requestId);
            log.debug("add redis lock, key={}, requestId={}, result={}",
                    this.key, this.requestId, result);
        } catch (Exception e) {
            log.error("try get distribute lock error", e);
        } finally {
            redisManager.releaseJedisClient(jedis);
        }
        return result;
    }

    @Override
    public boolean unlock() {
        boolean result = false;
        Jedis jedis = null;
        if(StringUtils.isBlank(this.requestId)) {
            log.debug("request id seem not init, release lock fail");
            return false;
        }
        try {
            jedis = redisManager.getJedisClient();
            result = unlock(jedis, this.key, this.requestId);
            log.debug("release redis lock, key={}, requestId={}, result={}",
                    this.key, this.requestId, result);
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
        long redisReturn = (long) jedis.eval(RELEASE_LOCK_LUA_SCRIPT,
                Collections.singletonList(lockKey),
                Collections.singletonList(requestId));
        log.debug("release lock lua script execute result = {}", redisReturn);
        result = (1 == redisReturn);
        if (result) {
            isLocked.set(false);
        }
        return result;
    }
}
