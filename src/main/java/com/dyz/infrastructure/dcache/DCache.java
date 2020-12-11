package com.dyz.infrastructure.dcache;

public interface DCache {

    Object getCache(String key);

    void setCache(String key, Object value);

    void setCache(String key, Object value, int expired);

    void deleteCache(String key);
}