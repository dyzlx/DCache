package com.dyz.infrastructure.dcache.serializer;

import com.dyz.infrastructure.dcache.DCacheSerializer;
import com.dyz.infrastructure.dcache.exception.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectDCacheSerializer implements DCacheSerializer<Object> {

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if(object == null) {
            return new byte[0];
        }
        if(!(object instanceof Serializable)) {
            throw new SerializationException("serialize error, source object must be a Serializable type");
        }
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream)){
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            return byteStream.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("serialize error", e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if(bytes == null) {
            return null;
        }
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(byteStream)) {
            return objectInputStream.readObject();
        } catch (Exception e) {
            throw new SerializationException("deserialize error", e);
        }
    }
}
