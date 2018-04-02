package ibsp.cache.client.structure;

import java.util.Map;

import ibsp.cache.client.utils.CONSTS.DataType;
import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class HGetall4Bit extends Operate<Map<byte[], byte[]>, BinaryJedisCommands> {

	public HGetall4Bit() {
		command = "HGETALL";
		dataType = DataType.HASH;
		operateType = OperateType.READ;
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public Map<byte[], byte[]> doExecute(BinaryJedisCommands jedis) throws Exception {
		Map<byte[], byte[]> result = jedis.hgetAll(SafeEncoder.encode(getKey()));
		if(result!=null) {		    
    		long len = 0;
    		for(Map.Entry<byte[], byte[]> entry : result.entrySet()) {
    			len += (entry.getKey().length + entry.getValue().length);
    		}
    		setRespLength(len);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
}
