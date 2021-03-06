package com.dyz.infrastructure.dcache.serializer;

import com.dyz.infrastructure.dcache.exception.SerializationException;

public interface DCacheSerializer<T> {

    byte[] serialize(T t) throws SerializationException;

    T deserialize(byte[] bytes) throws SerializationException;
}
