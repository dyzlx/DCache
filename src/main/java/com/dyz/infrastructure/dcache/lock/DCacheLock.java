package com.dyz.infrastructure.dcache.lock;

public interface DCacheLock {

    boolean lock();

    boolean unlock();
}
