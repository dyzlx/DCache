package com.dyz.infrastructure.dcache;

public interface DCacheHandler {

    Object getCache(String key);

    void setCache(String key, Object value);

    void setCache(String key, Object value, long expired);

    void deleteCache(String key);
}
