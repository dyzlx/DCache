package com.dyz.infrastructure.dcache.connect;


import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Objects;
import java.util.UUID;

@Slf4j
public class RedisManager {

    private JedisPool jedisPool;

    private String id;

    public RedisManager(JedisPool jedisPool) {
        id = UUID.randomUUID().toString();
        this.jedisPool = jedisPool;
        log.info("init redis manager, id={}", id);
    }

    public String getClientId() {
        return this.id;
    }

    public void execute(RunWithJedis caller) {
        try (Jedis jedis = jedisPool.getResource()) {
            caller.run(jedis);
        } catch (JedisConnectionException e) {
            // TODO retry
        } catch (Exception e) {
            log.error("error in execute with jedis", e);
        }
    }

    public <R> R executeAndReturn(CallWithJedis<R> caller) {
        try (Jedis jedis = jedisPool.getResource()) {
            return caller.call(jedis);
        } catch (JedisConnectionException e) {
            // TODO retry
        } catch (Exception e) {
            log.error("error in execute with jedis", e);
        }
        return null;
    }

    /*
    public void execute(CallWithJedis caller) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            caller.call(jedis);
        } catch (Exception e) {
            log.error("error in get jedis client", e);
        } finally {
            this.releaseJedisClient(jedis);
        }
    }
    */

    public Jedis getJedisClient() {
        Jedis jedis = null;
        try {
            if (Objects.nonNull(jedisPool)) {
                jedis = jedisPool.getResource();
            }
        } catch (Exception e) {
            log.error("error in get jedis client", e);
        }
        if (Objects.isNull(jedis)) {
            throw new NullPointerException("Redis pool cannot be NULL.");
        }
        return jedis;
    }

    public void releaseJedisClient(Jedis client) {
        try {
            if (Objects.nonNull(client) && client.isConnected()) {
                client.close();
            }
        } catch (Exception e) {
            log.error("error in release redis connection", e);
        }
    }

    public void completelyShutDownClient(Jedis client) {
        try {
            if (Objects.nonNull(client) && client.isConnected()) {
                client.quit();
                client.disconnect();
                client.close();
            }
        } catch (Exception e) {
            log.error("error in shutdown redis connection", e);
        }
    }
}
