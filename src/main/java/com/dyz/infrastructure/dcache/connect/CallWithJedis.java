package com.dyz.infrastructure.dcache.connect;

import redis.clients.jedis.Jedis;

@FunctionalInterface
public interface CallWithJedis<R> {
    R call(Jedis jedis);
}
