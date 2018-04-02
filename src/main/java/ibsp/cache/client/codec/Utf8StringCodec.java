package ibsp.cache.client.codec;

import java.nio.ByteBuffer;

import ibsp.cache.client.protocol.ByteUtil;

/**
 * 
 * @author Thinkpad
 * @version Revision 1.0.0
 *
 */
public class Utf8StringCodec extends RedisCodec<String, String> {
    @Override
    public String decodeKey(ByteBuffer bytes) {
        return ByteUtil.encode( decode(bytes) );
    }

    @Override
    public String decodeValue(ByteBuffer bytes) {
    	return ByteUtil.encode( decode(bytes) );
    }

    @Override
    public byte[] encodeKey(String key) {
        return encode(key);
    }

    @Override
    public byte[] encodeValue(String value) {
        return encode(value);
    }

    private byte[] decode(ByteBuffer bytes) {
//      System.err.println("bytes:" + ByteUtil.Bytes2HexString( bytes.position() ));    	
    	return bytes.array();
    }    	   
    
    private byte[] encode(String string) {
    	return ByteUtil.encode( string );
    }
}
