package com.dyz.infrastructure.dcache.lock.redis;

import com.dyz.infrastructure.dcache.config.RedisManager;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.Collections;


/**
 *
 * implements redis lock by Hash struct in redis.
 * support reentrant, implements it in redis server side.
 *
 * Key <Lock_Name> Field <UUID:ThreadId> Value <Count>(for reentrant)
 *
 * Better than DRedisReentrantSimpleLock.
 *
 */
@Slf4j
public class DRedisLock extends AbstractDRedisLock{

    public DRedisLock(RedisManager redisManager, String name) {
        super(redisManager, name);
    }

    public DRedisLock(RedisManager redisManager, String name, long expireTime) {
        super(redisManager, name, expireTime);
    }

    @Override
    protected boolean lock(Jedis jedis, String lockKey, String lockIdentifier) {
        long luaReturn = (long) jedis.eval(
                "if (redis.call('exists', KEYS[1]) == 0)" +
                        " then" +
                        " redis.call('hincrby', KEYS[1], ARGV[1], 1);" +
                        " redis.call('pexpire', KEYS[1], ARGV[2]);" +
                        " return 1 end;" +
                        " if (redis.call('hexists', KEYS[1], ARGV[1]) == 1)" +
                        " then" +
                        " redis.call('hincrby', KEYS[1], ARGV[1], 1);" +
                        " redis.call('pexpire', KEYS[1], ARGV[2]);" +
                        " return 1 end;" +
                        " return redis.call('pttl', KEYS[1]);",
                Collections.singletonList(lockKey),
                Arrays.asList(lockIdentifier, String.valueOf(this.expireTime)));
        log.debug("acquire redis lock lua script result={}", luaReturn);
        return luaReturn == 1;
    }

    @Override
    protected boolean unlock(Jedis jedis, String lockKey, String lockIdentifier) {
        long luaReturn = (long) jedis.eval(
                "if (redis.call('hexists', KEYS[1], ARGV[1]) == 0)" +
                        " then return 1 end;" +
                        " local counter = redis.call('hincrby', KEYS[1], ARGV[1], -1);" +
                        " if (counter > 0)" +
                        " then" +
                        " redis.call('pexpire', KEYS[1], ARGV[2]);" +
                        " return 1;" +
                        " else" +
                        " redis.call('del', KEYS[1]);" +
                        " return 1 end;" +
                        " return -1;",
                Collections.singletonList(lockKey),
                Arrays.asList(lockIdentifier, String.valueOf(this.expireTime)));
        log.debug("release redis lock lua script result={}", luaReturn);
        return luaReturn == 1;
    }
}
