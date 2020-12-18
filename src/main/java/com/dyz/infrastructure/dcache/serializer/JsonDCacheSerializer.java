package com.dyz.infrastructure.dcache.serializer;

import com.dyz.infrastructure.dcache.exception.SerializationException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

public class JsonDCacheSerializer implements DCacheSerializer<Object> {

    private final ObjectMapper objectMapper;

    public JsonDCacheSerializer() {
        this(new ObjectMapper());
    }

    public JsonDCacheSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
    }

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if(object == null) {
            return new byte[0];
        }
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (Exception e) {
            throw new SerializationException("serialize error", e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if(bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, Object.class);
        } catch (Exception e) {
            throw new SerializationException("deserialize error", e);
        }
    }
}
