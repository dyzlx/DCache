package com.dyz.infrastructure.dcache.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class JDKLock implements DCacheLock {

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
        try {
            return lock.tryLock(tryLockTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("jdk lock error", e);
        }
        return false;
    }

    @Override
    public boolean unlock() {
        try {
            lock.unlock();
            return true;
        } catch (Exception e) {
            log.error("jdk unlock error", e);
        }
        return false;
    }
}
