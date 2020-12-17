package com.dyz.infrastructure.dcache.lock;

import com.dyz.infrastructure.dcache.impl.redis.RedisManager;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class RedisReentrantLock extends RedisLock{

    /**
     * support Reentrant redis lock
     */
    private static final ThreadLocal<Map<String, Integer>> THREAD_LOCK_LOCAL= new ThreadLocal<>();

    public RedisReentrantLock(RedisManager redisManager) {
        super(redisManager);
    }

    @Override
    protected boolean lock(Jedis jedis, String lockKey, String requestId) {
        boolean result;
        Map<String, Integer> currentThreadLockMap = THREAD_LOCK_LOCAL.get();
        Integer lockCount = currentThreadLockMap.get(lockKey);
        if(Objects.nonNull(lockCount) && lockCount > 0) {
            currentThreadLockMap.put(lockKey, lockCount+1);
            return true;
        }
        result = super.lock(jedis, lockKey, requestId);
        if(result) {
            currentThreadLockMap.put(lockKey, 1);
        }
        return result;
    }

    @Override
    protected boolean unlock(Jedis jedis, String lockKey, String requestId) {
        Map<String, Integer> currentThreadLockMap = THREAD_LOCK_LOCAL.get();
        Integer lockCount = currentThreadLockMap.get(lockKey);
        if(Objects.nonNull(lockCount)) {
            lockCount -= 1;
            if(lockCount > 0) {
                currentThreadLockMap.put(lockKey, lockCount);
                return true;
            } else {
                boolean result = super.unlock(jedis, lockKey, requestId);
                if(result) {
                    THREAD_LOCK_LOCAL.remove();
                }
                return result;
            }
        }
        return false;
    }
}
