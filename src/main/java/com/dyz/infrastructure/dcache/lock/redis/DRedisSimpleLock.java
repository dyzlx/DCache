package com.dyz.infrastructure.dcache.lock.redis;

import com.dyz.infrastructure.dcache.impl.redis.RedisConstant;
import com.dyz.infrastructure.dcache.connect.RedisManager;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;

/**
 * implement redis lock by String struct in redis,
 * not support reentrant
 * if you want using reentrant lock, please use DRedisLock or DRedisSimpleLock
 *
 */
@Slf4j
public class DRedisSimpleLock extends AbstractDRedisLock {

    public DRedisSimpleLock(RedisManager redisManager, String name) {
        super(redisManager, name);
    }

    public DRedisSimpleLock(RedisManager redisManager, String name, long expireTime) {
        super(redisManager, name, expireTime);
    }

    @Override
    protected boolean lock(Jedis jedis, String lockKey, String lockIdentifier) {
        boolean result;
        // use a atomic command: set key value [EX seconds] [PX milliseconds] [NX|XX]
        String redisReturn = jedis.set(lockKey, lockIdentifier,
                SetParams.setParams().nx().px(this.expireTime));
        result = RedisConstant.SET_RETURN_SUCCESS.equals(redisReturn);
        return result;
    }

    @Override
    protected boolean unlock(Jedis jedis, String lockKey, String lockIdentifier) {
        boolean result;
        long redisReturn = (long) jedis.eval(
                "if redis.call('get',KEYS[1])==ARGV[1]" +
                        " then return redis.call('del',KEYS[1])" +
                        " else return 0 end",
                Collections.singletonList(lockKey),
                Collections.singletonList(lockIdentifier));
        log.debug("release lock lua script execute result = {}", redisReturn);
        result = (1 == redisReturn);
        return result;
    }
}
