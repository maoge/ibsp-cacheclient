package ibsp.cache.client.output;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;
import ibsp.cache.client.protocol.ScanResult;

public class ScanResultOutput<K, V> extends CommandOutput<K, V, ScanResult<Map<byte[], byte[]>>> {
	private Map<byte[], byte[]> mapResult;
	private byte[] key;
	
    public ScanResultOutput(RedisCodec<K, V> codec) {
        super(codec, new ScanResult<Map<byte[], byte[]>>());
    }

    @Override
    public void set(ByteBuffer bytes) {
    	if(output.getCursor()==null) {
    		output.setCursor( bytes == null ? null : bytes.array() );
    		return;    		
    	}
        if(mapResult==null) {
        	mapResult = new HashMap<byte[], byte[]>();
            output.setResults( mapResult );
        }
        if(key == null) {
            key = bytes.array();
            return;
        }
        byte[] value = (bytes == null) ? null : bytes.array();
        mapResult.put(key, value);
        key = null;    	
    }
}
