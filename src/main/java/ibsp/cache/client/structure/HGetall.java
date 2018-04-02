package ibsp.cache.client.structure;

import java.util.Map;

import redis.clients.util.SafeEncoder;
import ibsp.cache.client.core.NJedis;
import ibsp.cache.client.utils.CONSTS.DataType;

public class HGetall extends Operate<Map<String, String>, NJedis> {

	public HGetall() {
		command = "HGETALL";
		dataType = DataType.HASH;
		operateType = OperateType.READ;
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public Map<String, String> doExecute(NJedis jedis) throws Exception {
		Map<String, String> result = jedis.hgetAll(getKey());
	    if(result!=null) {      
	        long len = 0;
	        for(Map.Entry<String, String> entry : result.entrySet()) {
	            len += (entry.getKey().length() + entry.getValue().length());
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
