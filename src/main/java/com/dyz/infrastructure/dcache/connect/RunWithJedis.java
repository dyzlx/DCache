package com.dyz.infrastructure.dcache.connect;

import redis.clients.jedis.Jedis;

@FunctionalInterface
public interface RunWithJedis {
    void run(Jedis jedis);
}
