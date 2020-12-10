package com.dyz.infrastructure.dcache.handler;

import com.dyz.infrastructure.dcache.DCacheHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
//@Component
public class HashMapHandler implements DCacheHandler {

    @Override
    public Object getCache(String key) {
        return null;
    }

    @Override
    public void setCache(String key, Object value) {

    }

    @Override
    public void setCache(String key, Object value, long expired) {

    }

    @Override
    public void deleteCache(String key) {

    }
}
