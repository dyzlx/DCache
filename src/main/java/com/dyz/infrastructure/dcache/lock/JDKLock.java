package com.dyz.infrastructure.dcache.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class JDKLock implements DLock {

    private static final long DEFAULT_LOCK_TIMEOUT = 5 * 1000;

    private Lock lock = new ReentrantLock();

    private long tryLockTimeout;

    public JDKLock() {
        this(DEFAULT_LOCK_TIMEOUT);
    }

    public JDKLock(long timeout) {
        this.tryLockTimeout = timeout;
    }

    @Override
    public boolean lock() {
        boolean result = false;
        try {
            result = lock.tryLock(tryLockTimeout, TimeUnit.MILLISECONDS);
            log.debug("acquire jdk lock");
        } catch (InterruptedException e) {
            log.error("jdk lock acquire error", e);
        }
        return result;
    }

    @Override
    public boolean unlock() {
        try {
            lock.unlock();
            log.debug("release jdk lock");
            return true;
        } catch (Exception e) {
            log.error("jdk lock release error", e);
        }
        return false;
    }
}
