package com.dyz.infrastructure.dcache.lock;

public interface DLock {

    boolean lock();

    boolean unlock();
}
