package ibsp.cache.client.codec;

import java.nio.ByteBuffer;

/**
 * 
 * @author Thinkpad
 * @version Revision 1.0.0
 *
 */
public class Utf8ByteCodec extends RedisCodec<byte[], byte[]> {
    @Override
    public byte[] decodeKey(ByteBuffer bytes) {
    	return decode(bytes);
    }

    @Override
    public byte[] decodeValue(ByteBuffer bytes) {
    	return decode(bytes);
    }

    @Override
    public byte[] encodeKey(byte[] key) {
    	return key;
    }

    @Override
    public byte[] encodeValue(byte[] value) {
    	return value;
    }

    private byte[] decode(ByteBuffer bytes) {
    	return bytes.array();
    }    	   
}
