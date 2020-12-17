package com.dyz.infrastructure.dcache.serializer;

import com.dyz.infrastructure.dcache.DCacheSerializer;
import com.dyz.infrastructure.dcache.exception.SerializationException;
import org.apache.commons.lang3.StringUtils;

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
            return this.bytesToHexString(byteStream.toByteArray()).getBytes();
        } catch (Exception e) {
            throw new SerializationException("serialize error", e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if(bytes == null) {
            return null;
        }
        ByteArrayInputStream byteStream = new ByteArrayInputStream(this.hexStringToBytes(new String(bytes)));
        try (ObjectInputStream objectInputStream = new ObjectInputStream(byteStream)) {
            return objectInputStream.readObject();
        } catch (Exception e) {
            throw new SerializationException("deserialize error", e);
        }
    }

    /*
     if a byte[] b contains binary information,
     and byte[] c=new String(b).getBytes(),
     it is different between b and c
     */

    /*
     Converts a byte[] to a hexadecimal string
     */
    private String bytesToHexString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            return null;

        final StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if ((byteArray[i] & 0xff) < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return hexString.toString().toLowerCase();
    }

    /*
     Converts a hexadecimal string to byte[]
     */
    private byte[] hexStringToBytes(String hexString) {
        if (StringUtils.isBlank(hexString)) {
            return new byte[0];
        }
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }
}
